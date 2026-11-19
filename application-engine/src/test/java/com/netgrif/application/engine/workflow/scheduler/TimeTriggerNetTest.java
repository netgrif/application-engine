package com.netgrif.application.engine.workflow.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeTriggerNetTest {

    private static final String NET_RESOURCE = "/petriflow/time-trigger-test.xml";

    private Element root;

    @BeforeEach
    void loadNet() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(NET_RESOURCE)) {
            assertNotNull(stream, NET_RESOURCE + " is missing from the classpath");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Document document = factory.newDocumentBuilder().parse(stream);
            root = document.getDocumentElement();
        }
    }

    @Test
    void netVersionIsOneZeroThree() {
        assertEquals("1.0.3", childElements(root, "version").getFirst().getTextContent().trim());
    }

    @Test
    void transitionT1HasSingleTwoMinuteTimeTrigger() {
        assertTimeTriggerDelays("t1", "PT2M");
    }

    @Test
    void transitionT2HasSingleFiveMinuteTimeTrigger() {
        assertTimeTriggerDelays("t2", "PT5M");
    }

    @Test
    void transitionT3HasSingleThirtySecondTimeTrigger() {
        assertTimeTriggerDelays("t3", "PT30S");
    }

    @Test
    void transitionT4UserTaskHasNoTimeTrigger() {
        assertTimeTriggerDelays("t4");
    }

    @Test
    void transitionT5HasSingleTwoMinuteTimeTrigger() {
        assertTimeTriggerDelays("t5", "PT2M");
    }

    @Test
    void transitionT7HasSingleThirtyMinuteTimeTrigger() {
        assertTimeTriggerDelays("t7", "PT30M");
    }

    @Test
    void transitionT10HasSingleOneMinuteTimeTrigger() {
        assertTimeTriggerDelays("t10", "PT1M");
    }

    @Test
    void transitionT9HasSingleExactDateTimeTrigger() {
        List<Element> triggers = childElements(transition("t9"), "trigger");

        assertEquals(1, triggers.size(), "t9 must have one trigger");
        assertEquals("time", triggers.getFirst().getAttribute("type"));
        assertTrue(childElements(triggers.getFirst(), "delay").isEmpty(), "t9 must not have a delay");
        List<Element> exact = childElements(triggers.getFirst(), "exact");
        assertEquals(1, exact.size(), "t9 must have one exact element");
        assertEquals("2029-12-31T23:00:00", exact.getFirst().getTextContent().trim());
    }

    @Test
    void everyExactTriggerIsLocalDateTimeWithoutZoneOrOffset() {
        var exactElements = root.getElementsByTagName("exact");

        assertTrue(exactElements.getLength() > 0, "net must contain at least one exact trigger");
        for (int index = 0; index < exactElements.getLength(); index++) {
            String value = exactElements.item(index).getTextContent().trim();

            assertDoesNotThrow(() -> LocalDateTime.parse(value), "exact '" + value + "' must be parseable by LocalDateTime.parse");
            assertTrue(value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?"),
                    "exact '" + value + "' must not contain Z or an offset");
        }
    }

    @Test
    void transitionT11SchedulesT9FromDateTimeFieldAfterFinish() {
        List<Element> events = childElements(transition("t11"), "event");

        assertEquals(1, events.size(), "t11 must have one event");
        assertEquals("finish", events.getFirst().getAttribute("type"));
        List<Element> actions = childElements(events.getFirst(), "actions");
        assertEquals(1, actions.size(), "t11 finish event must have one actions block");
        assertEquals("post", actions.getFirst().getAttribute("phase"));
        String code = childElements(actions.getFirst(), "action").getFirst().getTextContent();
        assertTrue(code.contains("scheduleExecution("), "action must call scheduleExecution");
        assertTrue(code.contains("scheduleExecution(pair.task,"), "action must pass the task id from TaskPair.task");
        assertFalse(code.contains("pair.task.stringId"), "TaskPair.task is already a String id");
        assertTrue(code.contains("scheduleExecution(pair.task, useCase.getFieldValue('dateTime_0'))"), "action must pass dateTime_0 value directly");
        assertFalse(code.contains("atZone"), "action must not convert the value itself");
        assertFalse(code.contains("Date.from"), "action must not convert the value itself");
    }

    @Test
    void dateTimeFieldIsDeclaredAsDateTimeData() {
        Element data = childElements(root, "data").stream()
                .filter(candidate -> "dateTime_0".equals(idOf(candidate)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("data dateTime_0 not found"));

        assertEquals("dateTime", data.getAttribute("type"));
    }

    @Test
    void transitionT6HasOnlyAutoTrigger() {
        assertTriggerTypes("t6", "auto");
        assertTimeTriggerDelays("t6");
    }

    @Test
    void transitionsT4AndT8HaveNoTimeOrAutoTrigger() {
        assertTriggerTypes("t4");
        assertTriggerTypes("t8");
    }

    @Test
    void newTransitionsHaveExactlyOneTrigger() {
        for (String id : List.of("t6", "t7", "t9", "t10")) {
            assertEquals(1, childElements(transition(id), "trigger").size(), id + " must have one trigger");
        }
    }

    @Test
    void netContainsExactlyTransitionsT1ToT11() {
        List<String> ids = childElements(root, "transition").stream().map(this::idOf).toList();

        assertEquals(11, ids.size(), "transitions must be unique");
        assertEquals(Set.of("t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10", "t11"), Set.copyOf(ids));
    }

    @Test
    void netContainsExactlyTheExpectedPlaces() {
        List<String> ids = childElements(root, "place").stream().map(this::idOf).toList();

        assertEquals(9, ids.size(), "places must be unique");
        assertEquals(Set.of("p1", "p2", "p3", "p4", "p6", "p7", "p8", "p10", "p11"), Set.copyOf(ids));
    }

    @Test
    void newPlacesHaveLabelBeforeTokens() {
        for (String placeId : List.of("p4", "p6", "p7", "p8", "p10", "p11")) {
            Element place = place(placeId);
            List<String> order = childElements(place, "*").stream().map(Node::getLocalName).toList();

            assertTrue(order.indexOf("label") >= 0 && order.indexOf("label") < order.indexOf("tokens"),
                    "label must precede tokens in place " + idOf(place) + " but order was " + order);
        }
    }

    @Test
    void restartBranchCyclesBackToRestartPlace() {
        assertEquals(List.of("p4>t6", "t6>p3"),
                List.of("a11", "a24").stream().map(this::arcDescription).toList());
        assertEquals(List.of("p10>t7", "t7>p6", "p6>t8", "t8>p4"),
                List.of("a23", "a14", "a15", "a16").stream().map(this::arcDescription).toList());
    }

    @Test
    void everyPlaceHasLabelAndTokens() {
        for (Element place : childElements(root, "place")) {
            assertEquals(1, childElements(place, "label").size(), "place " + idOf(place) + " must have one label");
            assertEquals(1, childElements(place, "tokens").size(), "place " + idOf(place) + " must have one tokens element");
        }
    }

    @Test
    void labelPrecedesTokensInEveryPlace() {
        for (Element place : childElements(root, "place")) {
            List<String> order = childElements(place, "*").stream().map(Node::getLocalName).toList();

            assertTrue(order.indexOf("label") >= 0 && order.indexOf("tokens") >= 0,
                    "place " + idOf(place) + " lacks label or tokens");
            assertTrue(order.indexOf("label") < order.indexOf("tokens"),
                    "label must precede tokens in place " + idOf(place) + " but order was " + order);
        }
    }

    private void assertTimeTriggerDelays(String transitionId, String... expectedDelays) {
        Element transition = transition(transitionId);
        List<String> delays = new ArrayList<>();
        for (Element trigger : childElements(transition, "trigger")) {
            if ("time".equals(trigger.getAttribute("type"))) {
                List<Element> delayElements = childElements(trigger, "delay");
                assertEquals(1, delayElements.size(), "time trigger of " + transitionId + " must have one delay");
                delays.add(delayElements.getFirst().getTextContent().trim());
            }
        }

        assertEquals(List.of(expectedDelays), delays, "time trigger delays of " + transitionId);
    }

    private void assertTriggerTypes(String transitionId, String... expectedTypes) {
        List<String> types = childElements(transition(transitionId), "trigger").stream()
                .map(trigger -> trigger.getAttribute("type"))
                .toList();

        assertEquals(List.of(expectedTypes), types, "trigger types of " + transitionId);
    }

    private String arcDescription(String arcId) {
        Element arc = childElements(root, "arc").stream()
                .filter(candidate -> arcId.equals(idOf(candidate)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("arc " + arcId + " not found"));
        return childElements(arc, "sourceId").getFirst().getTextContent().trim()
                + ">" + childElements(arc, "destinationId").getFirst().getTextContent().trim();
    }

    private Element place(String id) {
        return childElements(root, "place").stream()
                .filter(place -> id.equals(idOf(place)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("place " + id + " not found"));
    }

    private Element transition(String id) {
        return childElements(root, "transition").stream()
                .filter(transition -> id.equals(idOf(transition)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("transition " + id + " not found"));
    }

    private String idOf(Element element) {
        return childElements(element, "id").getFirst().getTextContent().trim();
    }

    private List<Element> childElements(Element parent, String localName) {
        List<Element> result = new ArrayList<>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element && ("*".equals(localName) || localName.equals(element.getLocalName()))) {
                result.add(element);
            }
        }
        return result;
    }
}
