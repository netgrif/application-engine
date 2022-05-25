package com.netgrif.application.engine.importer;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ImporterTest {

    private static final String NET_ID = "prikladFM_test.xml";
    private static final String NET_TITLE = "Test";
    private static final String NET_INITIALS = "TST";
    private static final Integer NET_PLACES = 17;
    private static final Integer NET_TRANSITIONS = 22;
    private static final Integer NET_ARCS = 21;
    private static final Integer NET_FIELDS = 27;
    private static final Integer NET_ROLES = 3;
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

    @BeforeEach
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

        assert caseOutcome.getCase() != null;
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

    @Test
    void importInvalidDataRefLayoutTest() throws FileNotFoundException {
        LoggedUser loggedUser = superCreator.getLoggedSuper();
        assert  loggedUser != null;

        FileInputStream fileInputStream = new FileInputStream("src/test/resources/invalid_data_ref_layout.xml");

        assertThatThrownBy(() -> petriNetService.importPetriNet(fileInputStream, VersionType.MAJOR, loggedUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doesn't have a layout");

    }

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
