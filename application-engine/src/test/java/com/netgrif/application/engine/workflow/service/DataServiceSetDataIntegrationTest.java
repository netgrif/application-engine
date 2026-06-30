package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class DataServiceSetDataIntegrationTest {

    private static final String EDITABLE_TRANSITION = "1";
    private static final String NUMBER_CURRENCY_FIELD = "number_currency";
    private static final String ENUMERATION_LIST_FIELD = "enumeration_list";
    private static final String MULTICHOICE_LIST_FIELD = "multichoice_list";
    private static final String DATE_FIELD = "date";
    private static final String DATETIME_FIELD = "datetime";
    private static final String TEXT_FIELD = "text";

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private SuperCreatorRunner superCreator;

    private PetriNet net;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
        Optional<PetriNet> importedNet = importHelper.createNet("all_data.xml");
        assertTrue(importedNet.isPresent());
        net = importedNet.get();
    }

    @Test
    void setDataAcceptsEmptyNumberValueAndStoresNull() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                NUMBER_CURRENCY_FIELD,
                fieldWithValue("number", "")
        ));

        assertNotNull(outcome.getCase());
        assertTrue(outcome.getChangedFields().containsKey(NUMBER_CURRENCY_FIELD));
        assertNull(outcome.getCase().getFieldValue(NUMBER_CURRENCY_FIELD));
        assertNull(workflowService.findOne(useCase.getStringId()).getFieldValue(NUMBER_CURRENCY_FIELD));
    }

    @Test
    void setDataAcceptsNullEnumerationValueAndStoresNull() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                ENUMERATION_LIST_FIELD,
                fieldWithNullValue("enumeration")
        ));

        assertNotNull(outcome.getCase());
        assertTrue(outcome.getChangedFields().containsKey(ENUMERATION_LIST_FIELD));
        assertNull(outcome.getCase().getFieldValue(ENUMERATION_LIST_FIELD));
        assertNull(workflowService.findOne(useCase.getStringId()).getFieldValue(ENUMERATION_LIST_FIELD));
    }

    @Test
    void setDataTreatsStringNullEnumerationValueAsNull() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                ENUMERATION_LIST_FIELD,
                fieldWithValue("enumeration", "null")
        ));

        assertNotNull(outcome.getCase());
        assertNull(outcome.getCase().getFieldValue(ENUMERATION_LIST_FIELD));
    }

    @Test
    void setDataStoresValidNumberValue() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                NUMBER_CURRENCY_FIELD,
                fieldWithValue("number", "42.25")
        ));

        assertEquals(42.25d, (Double) outcome.getCase().getFieldValue(NUMBER_CURRENCY_FIELD), 0.001d);
        assertEquals(42.25d, (Double) workflowService.findOne(useCase.getStringId()).getFieldValue(NUMBER_CURRENCY_FIELD), 0.001d);
    }

    @Test
    void setDataStoresValidEnumerationValue() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                ENUMERATION_LIST_FIELD,
                fieldWithValue("enumeration", "Alice")
        ));

        I18nString value = assertInstanceOf(I18nString.class, outcome.getCase().getFieldValue(ENUMERATION_LIST_FIELD));
        assertEquals("Alice", value.getDefaultValue());
    }

    @Test
    void setDataAcceptsBlankDateAndNullDateTimeValuesAndStoresNull() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        ObjectNode dataSet = dataSet(DATE_FIELD, fieldWithValue("date", ""));
        dataSet.set(DATETIME_FIELD, fieldWithNullValue("dateTime"));

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet);

        assertTrue(outcome.getChangedFields().containsKey(DATE_FIELD));
        assertTrue(outcome.getChangedFields().containsKey(DATETIME_FIELD));
        assertNull(outcome.getCase().getFieldValue(DATE_FIELD));
        assertNull(outcome.getCase().getFieldValue(DATETIME_FIELD));
        Case persistedCase = workflowService.findOne(useCase.getStringId());
        assertNull(persistedCase.getFieldValue(DATE_FIELD));
        assertNull(persistedCase.getFieldValue(DATETIME_FIELD));
    }

    @Test
    void setDataStoresDateAndDateTimeValues() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        ObjectNode dataSet = dataSet(DATE_FIELD, fieldWithValue("date", "29.05.2026"));
        dataSet.set(DATETIME_FIELD, fieldWithValue("dateTime", "2026-05-29T13:45:10"));

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet);

        assertEquals(LocalDate.of(2026, Month.MAY, 29), outcome.getCase().getFieldValue(DATE_FIELD));
        assertEquals(LocalDateTime.of(2026, Month.MAY, 29, 13, 45, 10), outcome.getCase().getFieldValue(DATETIME_FIELD));
        Case persistedCase = workflowService.findOne(useCase.getStringId());
        assertStoredDate(LocalDate.of(2026, Month.MAY, 29), persistedCase.getFieldValue(DATE_FIELD));
        assertStoredDateTime(LocalDateTime.of(2026, Month.MAY, 29, 13, 45, 10), persistedCase.getFieldValue(DATETIME_FIELD));
    }

    @Test
    void setDataAcceptsStringNullMultichoiceValueAndStoresNull() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                MULTICHOICE_LIST_FIELD,
                fieldWithValue("multichoice", "null")
        ));

        assertTrue(outcome.getChangedFields().containsKey(MULTICHOICE_LIST_FIELD));
        assertNull(outcome.getCase().getFieldValue(MULTICHOICE_LIST_FIELD));
    }

    @Test
    void setDataStoresMultichoiceArrayAsI18nStringSet() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                MULTICHOICE_LIST_FIELD,
                fieldWithArrayValue("multichoice", "Alice", "Carol")
        ));

        Set<?> value = assertInstanceOf(Set.class, outcome.getCase().getFieldValue(MULTICHOICE_LIST_FIELD));
        assertEquals(
                Set.of("Alice", "Carol"),
                value.stream()
                        .map(item -> ((I18nString) item).getDefaultValue())
                        .collect(Collectors.toSet())
        );
        assertEquals(
                Set.of("Alice", "Carol"),
                multichoiceValues(workflowService.findOne(useCase.getStringId()).getFieldValue(MULTICHOICE_LIST_FIELD))
        );
    }

    @Test
    void setDataStoresTextValue() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                TEXT_FIELD,
                fieldWithValue("text", "Updated text")
        ));

        assertTrue(outcome.getChangedFields().containsKey(TEXT_FIELD));
        assertEquals("Updated text", outcome.getCase().getFieldValue(TEXT_FIELD));
        assertEquals("Updated text", workflowService.findOne(useCase.getStringId()).getFieldValue(TEXT_FIELD));
    }

    private Case createCase() {
        CreateCaseEventOutcome outcome = workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title("Set data integration test")
                .color("blue")
                .author(superCreator.getLoggedSuper())
                .build());
        assertNotNull(outcome.getCase());
        return outcome.getCase();
    }

    private Task findEditableTask(Case useCase) {
        return taskService.findAllByCase(useCase.getStringId()).stream()
                .filter(task -> EDITABLE_TRANSITION.equals(task.getTransitionId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Editable task was not created"));
    }

    private ObjectNode dataSet(String fieldId, ObjectNode field) {
        ObjectNode dataSet = JsonNodeFactory.instance.objectNode();
        dataSet.set(fieldId, field);
        return dataSet;
    }

    private ObjectNode fieldWithValue(String type, String value) {
        ObjectNode field = field(type);
        field.put("value", value);
        return field;
    }

    private ObjectNode fieldWithNullValue(String type) {
        ObjectNode field = field(type);
        field.putNull("value");
        return field;
    }

    private ObjectNode fieldWithArrayValue(String type, String... values) {
        ObjectNode field = field(type);
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (String value : values) {
            array.add(value);
        }
        field.set("value", array);
        return field;
    }

    private ObjectNode field(String type) {
        ObjectNode field = JsonNodeFactory.instance.objectNode();
        field.put("type", type);
        return field;
    }

    private void assertStoredDate(LocalDate expected, Object value) {
        if (value instanceof LocalDate actual) {
            assertEquals(expected, actual);
            return;
        }
        Date actual = assertInstanceOf(Date.class, value);
        assertEquals(expected, actual.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private void assertStoredDateTime(LocalDateTime expected, Object value) {
        if (value instanceof LocalDateTime actual) {
            assertEquals(expected, actual);
            return;
        }
        Date actual = assertInstanceOf(Date.class, value);
        assertEquals(expected, actual.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    private Set<String> multichoiceValues(Object value) {
        Collection<?> items = assertInstanceOf(Collection.class, value);
        return items.stream()
                .map(item -> ((I18nString) item).getDefaultValue())
                .collect(Collectors.toSet());
    }
}
