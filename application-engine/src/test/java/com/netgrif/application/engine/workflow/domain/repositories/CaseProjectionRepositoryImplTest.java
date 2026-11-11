package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.domain.repositories.projection.CaseProjection;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class CaseProjectionRepositoryImplTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private CaseRepository repository;

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
    void staticFieldsAlwaysPresentAndOnlyRequestedKeysLoaded() {
        Case created = createCase("projection");
        Optional<Case> found = repository.findProjectedByObjectId(created.get_id().getObjectId(),
                CaseProjection.ofDataFields(List.of("text", "number")));
        assertTrue(found.isPresent());
        Case c = found.get();
        assertTrue(c.isPartiallyLoaded());
        assertEquals(created.getStringId(), c.getStringId());
        assertEquals(created.getTitle(), c.getTitle());
        assertEquals(created.getPetriNetObjectId(), c.getPetriNetObjectId());
        assertEquals(created.getImmediateDataFields(), c.getImmediateDataFields());
        assertEquals(created.getTasks().size(), c.getTasks().size());
        assertEquals(created.getTasks().stream().map(t -> t.getTask()).collect(java.util.stream.Collectors.toSet()),
                c.getTasks().stream().map(t -> t.getTask()).collect(java.util.stream.Collectors.toSet()));
        assertEquals(created.getActivePlaces(), c.getActivePlaces());
        assertEquals(Set.of("text", "number"), c.getDataSet().keySet());
    }

    @Test
    void emptyKeySetLoadsNoDataSetEntries() {
        Case created = createCase("empty");
        Case c = repository.findProjectedByObjectId(created.get_id().getObjectId(), CaseProjection.ofDataFields(List.of())).orElseThrow();
        assertTrue(c.getDataSet().isEmpty());
        assertEquals("empty", c.getTitle());
    }

    @Test
    void invalidKeysAreRejected() {
        ObjectId id = createCase("invalid").get_id().getObjectId();
        for (String bad : new String[]{"$where", "a.b", " ", "", "a\u0000b"}) {
            CaseProjection projection = new CaseProjection(Set.of(bad));
            assertThrows(IllegalArgumentException.class, () -> repository.findProjectedByObjectId(id, projection), bad);
            assertThrows(IllegalArgumentException.class, () -> repository.findAllProjectedByObjectIds(List.of(id), projection), bad);
        }
    }

    @Test
    void findAllProjectedReturnsRightDocumentsAndEmptyInputReturnsEmpty() {
        Case a = createCase("a");
        Case b = createCase("b");
        createCase("c");
        List<Case> result = repository.findAllProjectedByObjectIds(
                List.of(a.get_id().getObjectId(), b.get_id().getObjectId()), CaseProjection.ofDataFields(List.of("text")));
        assertEquals(2, result.size());
        assertEquals(Set.of(a.getStringId(), b.getStringId()), Set.of(result.get(0).getStringId(), result.get(1).getStringId()));
        result.forEach(c -> assertEquals(Set.of("text"), c.getDataSet().keySet()));
        assertTrue(repository.findAllProjectedByObjectIds(List.of(), CaseProjection.ofDataFields(List.of("text"))).isEmpty());
    }

    @Test
    void predicateProjectedQueriesFindDocuments() {
        Case created = createCase("predicate-test");
        Optional<Case> one = repository.findOneProjected(QCase.case$.title.eq("predicate-test"),
                CaseProjection.ofDataFields(List.of("text")));
        assertTrue(one.isPresent());
        assertTrue(one.get().isPartiallyLoaded());
        assertEquals("predicate-test", one.get().getTitle());
        assertEquals(Set.of("text"), one.get().getDataSet().keySet());

        List<Case> list = repository.findAllProjected(QCase.case$.title.eq("predicate-test"),
                CaseProjection.ofDataFields(List.of("text")));
        assertEquals(1, list.size());
        assertTrue(list.get(0).isPartiallyLoaded());

        var page = repository.findAllProjected(QCase.case$.title.eq("predicate-test"),
                org.springframework.data.domain.PageRequest.of(0, 10),
                CaseProjection.ofDataFields(List.of("text")));
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertTrue(page.getContent().get(0).isPartiallyLoaded());
    }
}
