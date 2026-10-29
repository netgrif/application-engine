package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.domain.repositories.projection.CaseProjection;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.TaskController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ProjectionTestNetIntegrationTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private TaskController taskController;

    @Autowired
    private SuperCreatorRunner superCreator;

    @Autowired
    private MongoTemplate mongoTemplate;

    private PetriNet net;

    @BeforeEach
    void setUp() {
        testHelper.truncateDbs();
        net = importHelper.createNet("projection_test_net.xml").orElseThrow(
                () -> new IllegalStateException("Failed to import projection_test_net.xml")
        );
    }

    private Case createTestCase(String title) {
        return workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title(title)
                .color("indigo")
                .author(superCreator.getLoggedSuper())
                .build()).getCase();
    }

    @Test
    @DisplayName("PetriNet imports successfully with all data types and transitions")
    void testPetriNetImportAndFullCase() {
        assertNotNull(net);
        assertEquals("projection_test_net", net.getIdentifier());

        // Verify dataset covers all expected field types
        Set<String> fieldKeys = net.getDataSet().keySet();
        assertTrue(fieldKeys.containsAll(List.of(
                "txt_simple", "txt_area", "txt_password",
                "num_standard", "num_currency", "num_slider",
                "bool_flag", "bool_opt_in",
                "dt_birth", "dtt_scheduled",
                "enum_dropdown", "enum_autocomplete", "enum_map_status",
                "multi_tags", "multi_map_roles",
                "file_document", "file_gallery",
                "actor_assignee", "actor_reviewers",
                "ref_related_cases", "ref_embedded_task", "filter_query",
                "btn_action", "i18n_notice", "i18n_divider", "str_list_keywords"
        )));

        // Full load returns entire dataset
        Case created = createTestCase("Full Case Test");
        Case full = workflowService.findOne(created.getStringId());
        assertFalse(full.isPartiallyLoaded());
        assertTrue(full.getDataSet().keySet().containsAll(fieldKeys));
    }

    @Test
    @DisplayName("findOneProjected loads only transition subsets and required immediate fields")
    void testFindOneProjectedSubsets() {
        Case created = createTestCase("Projection Subset Test");

        // 1. Transition 1: Basic Form Fields
        Collection<String> t1Fields = List.of("txt_simple", "txt_area", "num_standard", "num_currency", "bool_flag", "dt_birth");
        Case partialT1 = workflowService.findOneProjected(created.getStringId(), t1Fields);

        assertTrue(partialT1.isPartiallyLoaded());
        // Requested fields plus any immediate fields must be present
        assertTrue(partialT1.getDataSet().keySet().containsAll(t1Fields));
        // Non-immediate fields from other transitions must not be loaded
        assertFalse(partialT1.getDataSet().containsKey("txt_password"));
        assertFalse(partialT1.getDataSet().containsKey("num_slider"));
        assertFalse(partialT1.getDataSet().containsKey("bool_opt_in"));
        assertFalse(partialT1.getDataSet().containsKey("ref_related_cases"));
        assertFalse(partialT1.getDataSet().containsKey("filter_query"));

        // 2. Transition 2: Choices and Maps
        Collection<String> t2Fields = List.of("enum_dropdown", "enum_autocomplete", "enum_map_status", "multi_tags", "multi_map_roles");
        Case partialT2 = workflowService.findOneProjected(created.getStringId(), t2Fields);

        assertTrue(partialT2.isPartiallyLoaded());
        assertTrue(partialT2.getDataSet().keySet().containsAll(t2Fields));
        assertFalse(partialT2.getDataSet().containsKey("num_slider"));
        assertFalse(partialT2.getDataSet().containsKey("ref_related_cases"));
    }

    @Test
    @DisplayName("DataService.getData loads projected case matching transition dataset")
    void testDataServiceGetDataWithProjection() {
        Case created = createTestCase("DataService Projection Test");

        // Locate task for t1_basic
        Task t1Task = created.getTasks().stream()
                .map(ref -> taskService.findOne(ref.getTask()))
                .filter(t -> "t1_basic".equals(t.getTransitionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Task t1_basic not found"));

        GetDataEventOutcome outcome = dataService.getData(t1Task.getStringId());
        assertNotNull(outcome);
        assertTrue(outcome.getCase().isPartiallyLoaded());

        List<Field<?>> dataFields = outcome.getData();
        assertNotNull(dataFields);
        assertFalse(dataFields.isEmpty());

        Set<String> outcomeFieldIds = dataFields.stream().map(Field::getStringId).collect(java.util.stream.Collectors.toSet());
        assertTrue(outcomeFieldIds.containsAll(List.of("txt_simple", "txt_area", "num_standard", "num_currency", "bool_flag", "dt_birth")));
        assertFalse(outcomeFieldIds.contains("enum_dropdown"));
        assertFalse(outcomeFieldIds.contains("file_document"));
    }

    @Test
    @DisplayName("DataService.getDataGroups resolves data groups and fields on projected case")
    void testDataServiceGetDataGroupsWithProjection() {
        Case created = createTestCase("DataGroups Projection Test");

        Task t1Task = created.getTasks().stream()
                .map(ref -> taskService.findOne(ref.getTask()))
                .filter(t -> "t1_basic".equals(t.getTransitionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Task t1_basic not found"));

        GetDataGroupsEventOutcome outcome = dataService.getDataGroups(t1Task.getStringId(), Locale.ENGLISH);
        assertNotNull(outcome);
        assertTrue(outcome.getCase().isPartiallyLoaded());
        assertNotNull(outcome.getData());
        assertFalse(outcome.getData().isEmpty());
    }

    @Test
    @DisplayName("TaskController.getData returns projected outcome successfully")
    void testTaskControllerGetData() {
        Case created = createTestCase("TaskController Projection Test");

        Task t1Task = created.getTasks().stream()
                .map(ref -> taskService.findOne(ref.getTask()))
                .filter(t -> "t1_basic".equals(t.getTransitionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Task t1_basic not found"));

        var entityModel = taskController.getData(t1Task.getStringId(), Locale.ENGLISH);
        assertNotNull(entityModel);
        assertNotNull(entityModel.getContent());
        assertNull(entityModel.getContent().getError());
        assertNotNull(entityModel.getContent().getOutcome());
    }

    @Test
    @DisplayName("savePartial updates loaded dataSet keys without deleting unloaded fields in Mongo")
    void testSavePartialPreservesUnloadedKeys() {
        Case created = createTestCase("Partial Save Test");

        // Load partially with only txt_simple and num_standard
        Case partial = workflowService.findOneProjected(created.getStringId(), List.of("txt_simple", "num_standard"));
        assertTrue(partial.isPartiallyLoaded());

        // Update txt_simple and num_standard
        partial.getDataSet().get("txt_simple").setValue("Modified text in partial save");
        partial.getDataSet().get("num_standard").setValue(999.0);

        // Plain save throws guard exception
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> workflowService.save(partial));
        assertTrue(ex.getMessage().contains("savePartial"));

        // Save via savePartial
        Case saved = workflowService.savePartial(partial);
        assertNotNull(saved);

        // Reload full case from MongoDB to verify that unloaded fields remain intact
        Case fullAfterUpdate = workflowService.findOne(created.getStringId());
        assertFalse(fullAfterUpdate.isPartiallyLoaded());

        // Verify updated values
        assertEquals("Modified text in partial save", fullAfterUpdate.getDataSet().get("txt_simple").getValue());
        assertEquals(999.0, fullAfterUpdate.getDataSet().get("num_standard").getValue());

        // Verify that fields not present in partial load are still intact with their initial values
        assertNotNull(fullAfterUpdate.getDataSet().get("enum_dropdown"));
        assertEquals("MEDIUM", fullAfterUpdate.getDataSet().get("enum_dropdown").getValue().toString());
        assertNotNull(fullAfterUpdate.getDataSet().get("bool_flag"));
        assertEquals(true, fullAfterUpdate.getDataSet().get("bool_flag").getValue());
    }

    @Test
    @DisplayName("DataService.setData updates fields and preserves unloaded dataset keys in Mongo")
    void testDataServiceSetDataWithProjection() {
        Case created = createTestCase("SetData Projection Test");

        Task t1Task = created.getTasks().stream()
                .map(ref -> taskService.findOne(ref.getTask()))
                .filter(t -> "t1_basic".equals(t.getTransitionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Task t1_basic not found"));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode values = mapper.createObjectNode();
        ObjectNode txtNode = values.putObject("txt_simple");
        txtNode.put("type", "text");
        txtNode.put("value", "Updated via SetData");

        ObjectNode numNode = values.putObject("num_standard");
        numNode.put("type", "number");
        numNode.put("value", 123.45);

        SetDataEventOutcome outcome = dataService.setData(t1Task.getStringId(), values);
        assertNotNull(outcome);
        assertNotNull(outcome.getCase());

        // Full case reload from Mongo to verify that modified fields updated and untouched fields remain intact
        Case full = workflowService.findOne(created.getStringId());
        assertEquals("Updated via SetData", full.getDataSet().get("txt_simple").getValue());
        assertEquals(123.45, full.getDataSet().get("num_standard").getValue());
        assertEquals("MEDIUM", full.getDataSet().get("enum_dropdown").getValue().toString());
    }

    @Test
    @DisplayName("Querydsl searchProjected retrieves matching cases with projected subsets")
    void testQuerydslSearchProjected() {
        Case created = createTestCase("Search Predicate Test Net");

        // searchOneProjected with explicit keys
        Case found = workflowService.searchOneProjected(
                QCase.case$.title.eq("Search Predicate Test Net"),
                List.of("enum_dropdown", "multi_tags")
        );
        assertNotNull(found);
        assertTrue(found.isPartiallyLoaded());
        assertEquals("Search Predicate Test Net", found.getTitle());
        assertTrue(found.getDataSet().containsKey("enum_dropdown"));
        assertTrue(found.getDataSet().containsKey("multi_tags"));
        assertFalse(found.getDataSet().containsKey("txt_password"));

        // searchProjected with Pageable
        Page<Case> paged = workflowService.searchProjected(
                QCase.case$.title.eq("Search Predicate Test Net"),
                PageRequest.of(0, 10),
                List.of("txt_simple")
        );
        assertEquals(1, paged.getTotalElements());
        assertTrue(paged.getContent().get(0).isPartiallyLoaded());
        assertTrue(paged.getContent().get(0).getDataSet().containsKey("txt_simple"));
        assertFalse(paged.getContent().get(0).getDataSet().containsKey("num_slider"));

        // searchAllProjected
        Page<Case> allProjected = workflowService.searchAllProjected(
                QCase.case$.title.eq("Search Predicate Test Net"),
                List.of("txt_simple")
        );
        assertEquals(1, allProjected.getTotalElements());
        assertTrue(allProjected.getContent().get(0).isPartiallyLoaded());
    }

    @Test
    @DisplayName("Independent embedded task transition resolves ref_embedded_task and remains valid across flow executions")
    void testEmbeddedTaskRefWithIndependentTransition() throws Exception {
        Case testCase = createTestCase("Embedded Task Ref Test");
        assertNotNull(testCase);

        // Check that ref_embedded_task was initialized to the task ID of t_embedded_task
        Task embeddedTask = findTaskByTransition(testCase.getStringId(), "t_embedded_task");
        assertNotNull(embeddedTask, "Task for independent transition t_embedded_task should be created on case creation");

        Case reloaded = workflowService.findOne(testCase.getStringId());
        List<String> taskRefValues = (List<String>) reloaded.getDataSet().get("ref_embedded_task").getValue();
        assertNotNull(taskRefValues);
        assertEquals(1, taskRefValues.size());
        assertEquals(embeddedTask.getStringId(), taskRefValues.get(0));

        // Finish t1_basic
        Task t1Task = findTaskByTransition(testCase.getStringId(), "t1_basic");
        assertNotNull(t1Task);
        taskService.assignTask(com.netgrif.application.engine.workflow.params.TaskParams.with().task(t1Task).user(superCreator.getLoggedSuper()).build());
        taskService.finishTask(com.netgrif.application.engine.workflow.params.TaskParams.with().task(t1Task).user(superCreator.getLoggedSuper()).build());

        // t4_actors_references is now enabled from p_mid
        Task t4Task = findTaskByTransition(testCase.getStringId(), "t4_actors_references");
        assertNotNull(t4Task, "Task t4_actors_references should be enabled after t1_basic completes");

        // Verify that getData / getDataGroups for t4_actors_references resolves without error
        var t4Outcome = taskController.getData(t4Task.getStringId(), Locale.ENGLISH);
        assertNotNull(t4Outcome);
        assertNull(t4Outcome.getContent().getError(), "t4_actors_references must resolve data without error");

        var t4Groups = dataService.getDataGroups(t4Task.getStringId(), Locale.ENGLISH);
        assertNotNull(t4Groups);
        assertFalse(t4Groups.getData().isEmpty());
    }

    private Task findTaskByTransition(String caseId, String transitionId) {
        Case c = workflowService.findOne(caseId);
        return c.getTasks().stream()
                .map(ref -> taskService.findOne(ref.getTask()))
                .filter(t -> transitionId.equals(t.getTransitionId()))
                .findFirst()
                .orElse(null);
    }
}
