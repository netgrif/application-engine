package com.netgrif.application.engine.migration

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.migration.helpers.CaseMigrationHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.DataField
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable;
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

    @BeforeEach
    void beforeEach() {
        testHelper.truncateDbs()

        ImportPetriNetEventOutcome netV1Outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/nae_2432_v1.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netV1Outcome.getNet() != null
        netV1 = netV1Outcome.getNet()

        ImportPetriNetEventOutcome netV2Outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/nae_2432_v2.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netV2Outcome.getNet() != null
        netV2 = netV2Outcome.getNet()

        (1..100).stream().parallel().forEach {
            workflowService.createCase(netV1.stringId, "Net V1 " + it, null, superCreator.loggedSuper, Locale.default)
        }
    }

    @Test
    void migrateCasesWithCursor() {
        List<Case> caseList = workflowService.search(QCase.case$.processIdentifier.eq("nae_2432"), Pageable.ofSize(100)).getContent()
        caseList.forEach {
            assert !it.dataSet.containsKey("income")
            assert !it.dataSet.containsKey("recreate_info_text")
            assert it.enabledRoles.size() == 0
            assert it.tasks.size() == 1 && it.tasks[0].transition == "person_info"
        }

        caseMigrationHelper.updateCasesCursor({ Case useCase ->
            migrationHelper.updateCasePermissionsFromNet(useCase, netV2)
            migrationHelper.updateTasksPermissions(useCase, netV2, ["t1", "t2"])
            migrationHelper.reloadTasks(useCase, netV2)

            useCase.dataSet["income"] = new DataField(0)
            useCase.dataSet["recreate_info_text"] = new DataField("")

        }, "nae_2432")
        caseList = workflowService.search(QCase.case$.processIdentifier.eq("nae_2432"), Pageable.ofSize(100)).getContent()
        caseList.forEach {
            assert it.dataSet.containsKey("income")
            assert it.dataSet.containsKey("recreate_info_text")
            assert it.enabledRoles.size() == 5
            assert it.tasks.size() == 2 && it.tasks[0].transition == "person_info" && it.tasks[1].transition == "recreate_person"
        }
    }
}
