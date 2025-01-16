package com.netgrif.application.engine.importer;

import com.netgrif.application.engine.EngineTest;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TemplateCase;
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

    private LoggedUser loggedSuperUser = superCreator.getLoggedSuper();

    private static final String NET_ID = "prikladFM_test";
    private static final String NET_TITLE = "Test";
    private static final String NET_INITIALS = "TST";
    private static final Integer NET_PLACES = 17;
    private static final Integer NET_TRANSITIONS = 23;
    private static final Integer NET_ARCS = 21;
    private static final Integer NET_FIELDS = 27;
    private static final Integer NET_ROLES = 3;

    @Test
    public void importProcess() throws MissingProcessMetaDataException, IOException, MissingIconKeyException {
        processImportService.importProcess(new FileInputStream("src/test/resources/prikladFM_test.xml"),
                VersionType.MAJOR, loggedSuperUser);
        assertNetProperlyImported();
    }

    @Test
    public void priorityTest() throws MissingProcessMetaDataException, IOException, MissingIconKeyException {
        ImportProcessEventOutcome outcome = processImportService.importProcess(
                new FileInputStream("src/test/resources/priority_test.xml"), VersionType.MAJOR, loggedSuperUser);
        assert outcome.getTemplateCase() != null;
        assert outcome.getProcessScopedCase() != null;

        CreateCaseEventOutcome caseOutcome = workflowService.createCase(outcome.getTemplateCase().getStringId(),
                outcome.getTemplateCase().getTitle().getDefaultValue(), "color", loggedSuperUser);

        assert caseOutcome.getCase() != null;
    }

    @Test
    public void dataGroupTest() throws MissingProcessMetaDataException, IOException, MissingIconKeyException {
        ImportProcessEventOutcome outcome = processImportService.importProcess(
                new FileInputStream("src/test/resources/datagroup_test.xml"), VersionType.MAJOR, loggedSuperUser);

        assert outcome.getTemplateCase() != null;
    }

    @Test
    public void readArcImportTest() throws MissingProcessMetaDataException, IOException, MissingIconKeyException {
        processImportService.importProcess(new FileInputStream("src/test/resources/read_test.xml"),
                VersionType.MAJOR, loggedSuperUser);
    }

    private void assertNetProperlyImported() {
        assert templateCaseRepository.count() > 0;
        Page<TemplateCase> templateCases = templateCaseRepository.findByProcessIdentifier(NET_ID, new FullPageRequest());
        Case templateCase = templateCases.getContent().get(0);
        assert templateCase.getTitle().getDefaultValue().equals(NET_TITLE);
        assert templateCase.getProperties().get("initials").equals(NET_INITIALS);
        assert templateCase.getPlaces().size() == NET_PLACES;
        assert templateCase.getTransitions().size() == NET_TRANSITIONS;
        assert templateCase.getArcs().size() == NET_ARCS;
        assert templateCase.getDataSet().getFields().size() == NET_FIELDS;
//        assert templateCase.getRoles().size() == NET_ROLES;
    }
}
