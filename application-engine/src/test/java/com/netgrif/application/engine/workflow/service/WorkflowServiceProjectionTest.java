package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.TaskController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class WorkflowServiceProjectionTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private TaskController taskController;

    @Autowired
    private SuperCreatorRunner superCreator;

    private PetriNet net;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
        net = importHelper.createNet("all_data.xml").orElseThrow();
    }

    private Case createCase(String title) {
        return workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title(title)
                .color("blue")
                .author(superCreator.getLoggedSuper())
                .build()).getCase();
    }

    @Test
    void findOneProjectedAddsRequiredKeysAndSkipsOthers() {
        Case created = createCase("p");
        Case partial = workflowService.findOneProjected(created.getStringId(), List.of());
        assertTrue(partial.isPartiallyLoaded());
        assertEquals(created.getImmediateDataFields(), partial.getDataSet().keySet());
        assertFalse(partial.getDataSet().containsKey("userList1"));
        assertFalse(partial.getImmediateData().isEmpty());

        Case withExtra = workflowService.findOneProjected(created.getStringId(), List.of("userList1", "unknown_field"));
        assertTrue(withExtra.getDataSet().containsKey("userList1"));
        assertFalse(withExtra.getDataSet().containsKey("unknown_field"));
    }

    @Test
    void findOneProjectedUnknownIdThrows() {
        Case created = createCase("p");
        String missing = created.getStringId().replace(created.get_id().getObjectId().toString(), new org.bson.types.ObjectId().toString());
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOneProjected(missing, List.of()));
    }

    @Test
    void findAllByIdProjectedPreservesOrderAndSkipsUnknown() {
        Case a = createCase("a");
        Case b = createCase("b");
        Case c = createCase("c");
        String missing = a.getStringId().replace(a.get_id().getObjectId().toString(), new org.bson.types.ObjectId().toString());
        List<String> ids = List.of(c.getStringId(), missing, a.getStringId(), b.getStringId());
        List<Case> result = workflowService.findAllByIdProjected(ids, List.of("userList1"));
        assertEquals(List.of(c.getStringId(), a.getStringId(), b.getStringId()), result.stream().map(Case::getStringId).toList());
        result.forEach(r -> assertTrue(r.getDataSet().containsKey("userList1")));
    }

    @Test
    void savingPartialCaseThrows() {
        Case created = createCase("p");
        Case partial = workflowService.findOneProjected(created.getStringId(), List.of());
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> workflowService.save(partial));
        assertTrue(ex.getMessage().contains("must not be saved"));
    }

    @Test
    void findOneStillReturnsCompleteDataSet() {
        Case created = createCase("p");
        Case full = workflowService.findOne(created.getStringId());
        assertFalse(full.isPartiallyLoaded());
        assertTrue(full.getDataSet().keySet().containsAll(net.getDataSet().keySet()));
    }

    @Test
    void savePartialUpdatesOnlyLoadedKeysAndKeepsOthers() {
        Case created = createCase("p");
        Case partial = workflowService.findOneProjected(created.getStringId(), List.of("text"));
        partial.getDataSet().get("text").setValue("changed");
        partial.setTitle("new title");
        workflowService.savePartial(partial);

        Case full = workflowService.findOne(created.getStringId());
        assertEquals("changed", full.getDataSet().get("text").getValue());
        assertEquals("new title", full.getTitle());
        assertTrue(full.getDataSet().keySet().containsAll(net.getDataSet().keySet()));
        assertThrows(IllegalArgumentException.class, () -> workflowService.savePartial(full));
    }

    @Test
    void getAllProjectedPagesAndProjects() {
        long before = workflowService.getAll(org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        createCase("a");
        createCase("b");
        createCase("c");
        var page = workflowService.getAllProjected(org.springframework.data.domain.PageRequest.of(0, 2), List.of("userList1"));
        assertEquals(before + 3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        page.getContent().forEach(c -> {
            assertTrue(c.isPartiallyLoaded());
            if (c.getProcessIdentifier().equals(net.getIdentifier())) {
                assertTrue(c.getDataSet().containsKey("userList1"));
            }
        });
    }

    @Test
    void findOneProjectedWithResolverUsesNet() {
        Case created = createCase("p");
        Case partial = workflowService.findOneProjected(created.getStringId(), n -> List.of("userList1"));
        assertTrue(partial.getDataSet().containsKey("userList1"));
        assertTrue(partial.isPartiallyLoaded());
    }

    @Test
    void getDataGroupsUsesProjectedCase() {
        Case created = createCase("p");
        String taskId = created.getTasks().iterator().next().getTask();
        var outcome = dataService.getDataGroups(taskId, java.util.Locale.ENGLISH);
        assertNotNull(outcome);
        assertTrue(outcome.getCase().isPartiallyLoaded());
        assertNotNull(outcome.getData());
        assertFalse(outcome.getData().isEmpty());
    }

    @Test
    void taskControllerGetDataWorks() {
        Case created = createCase("p");
        String taskId = created.getTasks().iterator().next().getTask();
        var entityModel = taskController.getData(taskId, java.util.Locale.ENGLISH);
        assertNotNull(entityModel);
        assertNotNull(entityModel.getContent());
        assertNull(entityModel.getContent().getError());
        assertNotNull(entityModel.getContent().getOutcome());
    }

    @Test
    void searchProjectedMethodsReturnProjectedCases() {
        Case created = createCase("search-proj");
        Case foundOne = workflowService.searchOneProjected(QCase.case$.title.eq("search-proj"), List.of("text"));
        assertNotNull(foundOne);
        assertTrue(foundOne.isPartiallyLoaded());
        assertEquals("search-proj", foundOne.getTitle());
        assertTrue(foundOne.getDataSet().containsKey("text"));

        Case foundOneWithResolver = workflowService.searchOneProjected(QCase.case$.title.eq("search-proj"), n -> List.of("number"));
        assertNotNull(foundOneWithResolver);
        assertTrue(foundOneWithResolver.isPartiallyLoaded());
        assertTrue(foundOneWithResolver.getDataSet().containsKey("number"));

        var allProjected = workflowService.searchAllProjected(QCase.case$.title.eq("search-proj"), List.of("text"));
        assertEquals(1, allProjected.getTotalElements());
        assertTrue(allProjected.getContent().get(0).isPartiallyLoaded());

        var pagedProjected = workflowService.searchProjected(QCase.case$.title.eq("search-proj"),
                org.springframework.data.domain.PageRequest.of(0, 10), List.of("text"));
        assertEquals(1, pagedProjected.getTotalElements());
        assertTrue(pagedProjected.getContent().get(0).isPartiallyLoaded());
    }

    @Test
    void getDataWithEventsLoadsEntireDataSet() {
        PetriNet actionsNet = importHelper.createNet("data_actions_test.xml").orElseThrow();
        Case actionCase = workflowService.createCase(CreateCaseParams.with()
                .process(actionsNet)
                .title("Actions Case")
                .color("green")
                .author(superCreator.getLoggedSuper())
                .build()).getCase();
        String taskId = actionCase.getTasks().iterator().next().getTask();
        var outcome = dataService.getData(taskId);
        assertNotNull(outcome);
        assertTrue(outcome.getCase().isPartiallyLoaded());
        assertTrue(outcome.getCase().getDataSet().containsKey("control_field"));
        assertTrue(outcome.getCase().getDataSet().containsKey("text_field"));
    }
}
