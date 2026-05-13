package com.netgrif.application.engine.migration

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.migration.helpers.CaseMigrationHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.Duration
import java.time.LocalDateTime

@Slf4j
@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class MigrationBenchmarkTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private CaseMigrationHelper caseMigrationHelper

    @Autowired
    private MigrationHelper migrationHelper

    private PetriNet netV1, netV2

    private static FileWriter writer

    @BeforeAll
    static void beforeAll() {
        File report = new File("src/main/resources/migration_report.txt")
        if (report.createNewFile()) {
            log.info("New migration report file created")
        }
        writer = new FileWriter(report)
    }

    @BeforeEach
    void beforeEach() {
        testHelper.truncateDbs()

        ImportPetriNetEventOutcome netV1Outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/nae_2432_v1.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netV1Outcome.getNet() != null
        netV1 = netV1Outcome.getNet()

        ImportPetriNetEventOutcome netV2Outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/nae_2432_v2.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netV2Outcome.getNet() != null
        netV2 = netV2Outcome.getNet()

        (1..10000).stream().parallel().forEach {
            workflowService.createCase(netV1.stringId, "Net V1 " + it, null, superCreator.loggedSuper, Locale.default)
        }
    }

    @AfterAll
    static void afterAll() {
        writer.close()
    }

    //TODO: to be deleted
    @Test
    void migrateCasesWitLegacyCursor() {
        LocalDateTime startOfLegacyMigration = LocalDateTime.now()
        migrationHelper.updateCasesCursor({ Case useCase ->
            migrationHelper.updateCasePermissionsFromNet(useCase, netV2)
        }, "nae_2432")
        LocalDateTime endOfLegacyMigration = LocalDateTime.now()
        Duration diff = Duration.between(startOfLegacyMigration, endOfLegacyMigration)
        writer.write("==============================\n")
        writer.write("LEGACY MIGRATION HELPER\n")
        writer.write("Migrated 10000 cases\n")
        writer.write("Started at " + startOfLegacyMigration.toString() + "\n")
        writer.write("Ended at " + endOfLegacyMigration.toString() + "\n")
        writer.write("Duration: " + diff.toString() + "\n")
        writer.write("==============================\n")
    }

    @Test
    void migrateCasesWithCursor() {
        LocalDateTime startOfLegacyMigration = LocalDateTime.now()
        caseMigrationHelper.updateCasesCursor({ Case useCase ->
            migrationHelper.updateCasePermissionsFromNet(useCase, netV2)
        }, "nae_2432")
        LocalDateTime endOfLegacyMigration = LocalDateTime.now()
        Duration diff = Duration.between(startOfLegacyMigration, endOfLegacyMigration)
        writer.write("==============================\n")
        writer.write("NEW MIGRATION HELPER\n")
        writer.write("Migrated 10000 cases\n")
        writer.write("Started at " + startOfLegacyMigration.toString() + "\n")
        writer.write("Ended at " + endOfLegacyMigration.toString() + "\n")
        writer.write("Duration: " + diff.toString() + "\n")
        writer.write("==============================\n")
    }
}
