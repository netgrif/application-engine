package com.netgrif.workflow.importer;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.TaskServiceTest;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class ImporterTest {

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private IWorkflowService workflowService;

    private static final String NET_TITLE = "jaxb_test";
    private static final String NET_INITIALS = "TST";
    private static final Integer NET_PLACES = 17;
    private static final Integer NET_TRANSITIONS = 22;
    private static final Integer NET_ARCS = 21;
    private static final Integer NET_FIELDS = 27;
    private static final Integer NET_ROLES = 3;

    @Before
    public void before() {
        repository.deleteAll();
    }

    @Test
    public void importPetriNet() {
        importer.importPetriNet(new File("src/test/resources/prikladFM_test.xml"), NET_TITLE, NET_INITIALS);

        assertNetProperlyImported();
    }

    @Test
    public void priorityTest() {
        Optional<PetriNet> net = importer.importPetriNet(new File("src/test/resources/priority_test.xml"), "Priority test", "PT");

        assert net.isPresent();

        Case useCase = workflowService.createCase(net.get().getStringId(), net.get().getTitle().getDefaultValue(), "color", TaskServiceTest.mockLoggedUser());

        assert useCase != null;
    }

    @Test
    public void dataGroupTest() {
        Optional<PetriNet> net = importer.importPetriNet(new File("src/test/resources/datagroup_test.xml"), "DataGroup test", "DGT");

        assert net.isPresent();
    }

    @Test
    @Ignore
    public void caseRefTest() {
        importer.importPetriNet(new File("src/test/resources/datagroup_test.xml"), "DataGroup test", "DGT");
        Optional<PetriNet> net = importer.importPetriNet(new File("src/test/resources/caseref_test.xml"), "Caseref test", "CRT");
        assert net.isPresent();

        Case useCase = workflowService.createCase(net.get().getStringId(), net.get().getTitle().getDefaultValue(), "color", TaskServiceTest.mockLoggedUser());
        assert useCase != null;

        List<Field> data = workflowService.getData(useCase.getStringId());
        assert data != null && data.size() > 0;

        useCase.getDataSet().get(data.get(0).getStringId()).setValue(useCase.getStringId());
        workflowService.save(useCase);
        data = workflowService.getData(useCase.getStringId());
        assert data != null && data.size() > 0;
    }

    @Test
    public void readArcImportTest() {
        importer.importPetriNet(new File("src/test/resources/read_test.xml"), "R", "R");
    }

    @Test
    public void externalMappingTest() {
        Optional<PetriNet> net = importer.importPetriNet(new File("src/test/resources/mapping_test.xml"), "External mapping", "EXT");

        assertExternalMappingImport(net);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertExternalMappingImport(Optional<PetriNet> imported) {
        assert imported.isPresent();

        PetriNet net = imported.get();
        long[] noDataTransitions = {2,3,4,36,49};

        assert net.getPlaces().size() == 11;
        assert net.getTransitions().size() == 11;
        assert net.getArcs().values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()).size() == 34;
        assert net.getDataSet().size() == 14;
        assert net.getRoles().size() == 2;

        net.getTransitions().values().forEach(transition -> {
            assert !transition.getRoles().isEmpty();
            if (LongStream.of(noDataTransitions).anyMatch(x-> x == transition.getImportId())) {
                assert transition.getDataSet().isEmpty();
            } else {
                assert !transition.getDataSet().isEmpty();
            }
        });
    }

    private void assertNetProperlyImported() {
        assert repository.count() == 1;
        PetriNet net = repository.findAll().get(0);
        assert net.getTitle().equals(NET_TITLE);
        assert net.getInitials().equals(NET_INITIALS);
        assert net.getPlaces().size() == NET_PLACES;
        assert net.getTransitions().size() == NET_TRANSITIONS;
        assert net.getArcs().size() == NET_ARCS;
        assert net.getDataSet().size() == NET_FIELDS;
        assert net.getRoles().size() == NET_ROLES;
    }
}