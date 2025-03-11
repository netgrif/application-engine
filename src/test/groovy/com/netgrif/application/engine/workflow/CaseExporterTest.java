package com.netgrif.application.engine.workflow;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.CaseExporter;
import com.netgrif.application.engine.workflow.service.CaseImporter;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class CaseExporterTest {

    private final String testNetFileName = "all_data.xml";
    private final String testNetIdentifier = "all_data";
    private final String outputFileLocation = "src/test/resources/";
    private final String outputFileName = "case_export_test.xml";

    @Autowired
    private SuperCreatorRunner superCreator;

    @Autowired
    private CaseExporter caseExporter;

    @Autowired
    private PetriNetService petriNetService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private CaseImporter caseImporter;

    private PetriNet petriNet;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        try (FileInputStream fis = new FileInputStream("src/test/resources/" + testNetFileName)) {
            petriNet = petriNetService.importPetriNet(fis, VersionType.MAJOR, superCreator.getLoggedSuper()).getNet();
        } catch (MissingPetriNetMetaDataException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void exportCase() {
        Case toExport = workflowService.createCaseByIdentifier(testNetIdentifier, "export case", "", superCreator.getLoggedSuper()).getCase();
        try (FileOutputStream fos = new FileOutputStream(outputFileLocation + outputFileName)) {
            caseExporter.exportCases(List.of(toExport), fos);
        } catch (IOException e) {
            log.error("IO exception occured", e);
        }
        workflowService.deleteCase(toExport);
    }

    @Test
    public void importCase() {
        try (FileInputStream fis = new FileInputStream(outputFileLocation + outputFileName)) {
            List<Case> importedCases = caseImporter.importCases(fis);
            assert importedCases != null && !importedCases.isEmpty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}