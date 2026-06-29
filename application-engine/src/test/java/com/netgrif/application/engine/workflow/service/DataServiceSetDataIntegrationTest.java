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
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class DataServiceSetDataIntegrationTest {

    private static final String EDITABLE_TRANSITION = "1";
    private static final String NUMBER_CURRENCY_FIELD = "number_currency";
    private static final String ENUMERATION_LIST_FIELD = "enumeration_list";

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
    public void before() {
        testHelper.truncateDbs();
        Optional<PetriNet> importedNet = importHelper.createNet("all_data.xml");
        assertTrue(importedNet.isPresent());
        net = importedNet.get();
    }

    @Test
    public void setDataAcceptsEmptyNumberValueAndStoresNull() {
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
    public void setDataAcceptsNullEnumerationValueAndStoresNull() {
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
    public void setDataTreatsStringNullEnumerationValueAsNull() {
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
    public void setDataStoresValidNumberValue() {
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
    public void setDataStoresValidEnumerationValue() {
        Case useCase = createCase();
        Task task = findEditableTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task.getStringId(), dataSet(
                ENUMERATION_LIST_FIELD,
                fieldWithValue("enumeration", "Alice")
        ));

        I18nString value = assertInstanceOf(I18nString.class, outcome.getCase().getFieldValue(ENUMERATION_LIST_FIELD));
        assertEquals("Alice", value.getDefaultValue());
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

    private ObjectNode field(String type) {
        ObjectNode field = JsonNodeFactory.instance.objectNode();
        field.put("type", type);
        return field;
    }
}
