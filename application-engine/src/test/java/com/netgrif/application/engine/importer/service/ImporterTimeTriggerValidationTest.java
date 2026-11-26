package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.objects.importer.model.Trigger;
import com.netgrif.application.engine.objects.importer.model.TriggerType;
import com.netgrif.application.engine.objects.petrinet.domain.Transition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.datatype.DatatypeFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImporterTimeTriggerValidationTest {

    private Importer importer;
    private Transition transition;

    @BeforeEach
    void setUp() {
        importer = new Importer();
        ReflectionTestUtils.setField(importer, "triggerFactory", new TriggerFactory());
        transition = new Transition();
        transition.setImportId("t1");
    }

    @Test
    void acceptsSingleTimeTriggerNextToOtherTriggers() throws Exception {
        assertDoesNotThrow(() -> importer.addTrigger(transition, trigger(TriggerType.AUTO)));
        assertDoesNotThrow(() -> importer.addTrigger(transition, timeTrigger("PT2M")));

        assertEquals(2, transition.getTriggers().size());
    }

    @Test
    void rejectsSecondTimeTriggerWithTransitionImportIdAndCount() throws Exception {
        importer.addTrigger(transition, timeTrigger("PT2M"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> importer.addTrigger(transition, timeTrigger("PT5M")));

        assertEquals("Transition [t1] declares [2] time triggers, only one time trigger per transition is supported.", exception.getMessage());
    }

    private Trigger timeTrigger(String delay) throws Exception {
        Trigger trigger = trigger(TriggerType.TIME);
        trigger.setDelay(DatatypeFactory.newInstance().newDuration(delay));
        return trigger;
    }

    private Trigger trigger(TriggerType type) {
        Trigger trigger = new Trigger();
        trigger.setType(type);
        return trigger;
    }
}
