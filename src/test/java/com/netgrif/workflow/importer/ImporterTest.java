package com.netgrif.workflow.importer;

import com.netgrif.workflow.TestHelper;
import com.netgrif.workflow.importer.service.throwable.MissingIconKeyException;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.SuperCreator;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class ImporterTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    private static final String NET_ID = "prikladFM_test.xml";
    private static final String NET_TITLE = "Test";
    private static final String NET_INITIALS = "TST";
    private static final Integer NET_PLACES = 17;
    private static final Integer NET_TRANSITIONS = 22;
    private static final Integer NET_ARCS = 21;
    private static final Integer NET_FIELDS = 27;
    private static final Integer NET_ROLES = 3;

    @Before
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void importPetriNet() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/prikladFM_test.xml"), "major", superCreator.getLoggedSuper());
        assertNetProperlyImported();
    }

    @Test
    public void priorityTest() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        ImportPetriNetEventOutcome outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/priority_test.xml"), "major", superCreator.getLoggedSuper());
        assert outcome.getNet() != null;

        CreateCaseEventOutcome caseOutcome = workflowService.createCase(outcome.getNet().getStringId(), outcome.getNet().getTitle().getDefaultValue(), "color", superCreator.getLoggedSuper());

        assert caseOutcome.getACase() != null;
    }

    @Test
    public void dataGroupTest() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        ImportPetriNetEventOutcome outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/datagroup_test.xml"), "major", superCreator.getLoggedSuper());

        assert outcome.getNet() != null;
    }

    @Test
    public void readArcImportTest() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/read_test.xml"), "major", superCreator.getLoggedSuper());
    }

    @Test
    public void externalMappingTest() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        ImportPetriNetEventOutcome outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/mapping_test.xml"), "major", superCreator.getLoggedSuper());

        assertExternalMappingImport(outcome.getNet());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertExternalMappingImport(PetriNet imported) {
        assert imported != null;

        String[] noDataTransitions = {"2", "3", "4", "36", "49"};

        assert imported.getPlaces().size() == 11;
        assert imported.getTransitions().size() == 11;
        assert imported.getArcs().values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()).size() == 34;
        assert imported.getDataSet().size() == 14;
        assert imported.getRoles().size() == 2;

        imported.getTransitions().values().forEach(transition -> {
            assert !transition.getRoles().isEmpty();
            if (Arrays.stream(noDataTransitions).anyMatch(x -> x.equals(transition.getImportId()))) {
                assert transition.getDataSet().isEmpty();
            } else {
                assert !transition.getDataSet().isEmpty();
            }
        });
    }

    private void assertNetProperlyImported() {
        assert repository.count() > 0;
        Page<PetriNet> nets = repository.findByIdentifier(NET_ID, new FullPageRequest());
        PetriNet net = nets.getContent().get(0);
        assert net.getTitle().getDefaultValue().equals(NET_TITLE);
        assert net.getInitials().equals(NET_INITIALS);
        assert net.getPlaces().size() == NET_PLACES;
        assert net.getTransitions().size() == NET_TRANSITIONS;
        assert net.getArcs().size() == NET_ARCS;
        assert net.getDataSet().size() == NET_FIELDS;
        assert net.getRoles().size() == NET_ROLES;
    }
}