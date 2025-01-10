package com.netgrif.application.engine.importer;

import com.netgrif.application.engine.EngineTest;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.VersionType;
import com.netgrif.application.engine.workflow.domain.throwable.MissingProcessMetaDataException;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportProcessEventOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ImporterTest extends EngineTest {

    private static final String NET_ID = "prikladFM_test";
    private static final String NET_TITLE = "Test";
    private static final String NET_INITIALS = "TST";
    private static final Integer NET_PLACES = 17;
    private static final Integer NET_TRANSITIONS = 23;
    private static final Integer NET_ARCS = 21;
    private static final Integer NET_FIELDS = 27;
    private static final Integer NET_ROLES = 3;

    @Test
    public void importPetriNet() throws MissingProcessMetaDataException, IOException, MissingIconKeyException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/prikladFM_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        assertNetProperlyImported();
    }

    @Test
    public void priorityTest() throws MissingProcessMetaDataException, IOException, MissingIconKeyException {
        ImportProcessEventOutcome outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/priority_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        assert outcome.getNet() != null;

        CreateCaseEventOutcome caseOutcome = workflowService.createCase(outcome.getNet().getStringId(), outcome.getNet().getTitle().getDefaultValue(), "color", superCreator.getLoggedSuper());

        assert caseOutcome.getCase() != null;
    }

    @Test
    public void dataGroupTest() throws MissingProcessMetaDataException, IOException, MissingIconKeyException {
        ImportProcessEventOutcome outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/datagroup_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());

        assert outcome.getNet() != null;
    }

    @Test
    public void readArcImportTest() throws MissingProcessMetaDataException, IOException, MissingIconKeyException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/read_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
    }

    private void assertNetProperlyImported() {
        assert processRepository.count() > 0;
        Page<Process> nets = processRepository.findByIdentifier(NET_ID, new FullPageRequest());
        Process net = nets.getContent().get(0);
        assert net.getTitle().getDefaultValue().equals(NET_TITLE);
        assert net.getProperties().get("initials").equals(NET_INITIALS);
        assert net.getPlaces().size() == NET_PLACES;
        assert net.getTransitions().size() == NET_TRANSITIONS;
        assert net.getArcs().size() == NET_ARCS;
        assert net.getDataSet().size() == NET_FIELDS;
        assert net.getRoles().size() == NET_ROLES;
    }
}
