package com.netgrif.application.engine.importer;

import com.netgrif.application.engine.EngineTest;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.params.ImportProcessParams;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
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
    public void importPetriNet() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/prikladFM_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId()));
        assertNetProperlyImported();
    }

    @Test
    public void priorityTest() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        ImportPetriNetEventOutcome outcome = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/priority_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId()));
        assert outcome.getProcess() != null;

        TestHelper.login(superCreator.getSuperIdentity());
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(outcome.getProcess())
                .title(outcome.getProcess().getTitle().getDefaultValue())
                .authorId(superCreator.getLoggedSuper().getActiveActorId())
                .build();
        CreateCaseEventOutcome caseOutcome = workflowService.createCase(createCaseParams);

        assert caseOutcome.getCase() != null;
    }

    @Test
    public void dataGroupTest() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        ImportPetriNetEventOutcome outcome = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/datagroup_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId()));

        assert outcome.getProcess() != null;
    }

    @Test
    public void readArcImportTest() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/read_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId()));
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
    }
}
