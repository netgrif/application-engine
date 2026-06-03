package com.netgrif.application.engine.migration

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase
import com.netgrif.application.engine.adapter.spring.workflow.domain.QTask
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.migration.model.MigrationError
import com.netgrif.application.engine.migration.model.MigrationErrorPolicy
import com.netgrif.application.engine.migration.throwable.MigrationErrorException
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.DataField
import com.netgrif.application.engine.objects.workflow.domain.Task
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.workflow.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertThrows

@Slf4j
@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class MigrationTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Autowired
    private MigrationHelper migrationHelper

    @Autowired
    private ElasticCaseRepository elasticCaseRepository

    private PetriNet netV1, netV2

    private static final String MIGRATION_TEST_V1 = "petriNets/migration_test_v1.xml"

    private static final String MIGRATION_TEST_V2 = "petriNets/migration_test_v2.xml"

    @BeforeEach
    void beforeEach() {
        testHelper.truncateDbs()

        this.class.classLoader.getResourceAsStream(MIGRATION_TEST_V1).withCloseable { is ->
            ImportPetriNetParams importPetriNetParams = ImportPetriNetParams.with()
                    .xmlFile(is)
                    .releaseType(VersionType.MAJOR)
                    .author(superCreator.superUser)
                    .build()
            ImportPetriNetEventOutcome netV1Outcome = petriNetService.importPetriNet(importPetriNetParams)
            assert netV1Outcome.getNet() != null
            netV1 = netV1Outcome.getNet()
        }

        this.class.classLoader.getResourceAsStream(MIGRATION_TEST_V2).withCloseable { is ->
            ImportPetriNetParams importPetriNetParams = ImportPetriNetParams.with()
                    .xmlFile(is)
                    .releaseType(VersionType.MAJOR)
                    .author(superCreator.superUser)
                    .build()
            ImportPetriNetEventOutcome netV2Outcome = petriNetService.importPetriNet(importPetriNetParams)
            assert netV2Outcome.getNet() != null
            netV2 = netV2Outcome.getNet()
        }

        (1..10).forEach {
            CreateCaseParams caseParams = CreateCaseParams.with()
                    .processId(netV1.stringId)
                    .title("Net V1 " + it)
                    .author(superCreator.superUser)
                    .locale(Locale.default)
                    .build()
            workflowService.createCase(caseParams)
        }
    }

    @Test
    void migrationHelperShouldMigrateCasesAndReloadTasksThroughFacade() {
        List<Case> casesBeforeMigration = workflowService.search(
                QCase.case$.processIdentifier.eq("migration_test"),
                Pageable.ofSize(10)
        ).content

        assert casesBeforeMigration.size() == 10
        casesBeforeMigration.each { Case useCase ->
            assert !useCase.dataSet.containsKey("income")
            assert !useCase.dataSet.containsKey("recreate_info_text")
            assert useCase.enabledRoles.isEmpty()
            assert useCase.tasks.size() == 1
            assert useCase.tasks[0].transition == "person_info"
        }

        migrationHelper.withErrorPolicy(MigrationErrorPolicy.throwAfterProcessing()) {
            migrationHelper.updateCasesCursor({ Case useCase ->
                migrationHelper.removeCase(useCase)
                migrationHelper.updateCasePermissionsFromNet(useCase, netV2)
                migrationHelper.reloadTasks(useCase, netV2)
                migrationHelper.migratePetriNet(useCase, netV2)
                MigrationHelper.addTextDataFields(useCase, [
                        "recreate_info_text": ""
                ])
                useCase.dataSet["income"] = new DataField(1000)
            }, netV1.getObjectId(), 2)
        }

        List<Case> casesAfterMigration = workflowService.search(
                QCase.case$.processIdentifier.eq("migration_test"),
                Pageable.ofSize(10)
        ).content

        assert casesAfterMigration.size() == 10
        casesAfterMigration.each { Case useCase ->
            assert useCase.petriNetObjectId == netV2.objectId
            assert useCase.dataSet.containsKey("income")
            assert useCase.dataSet["income"].value == 1000
            assert useCase.dataSet.containsKey("recreate_info_text")
            assert useCase.enabledRoles.size() == 5
            assert useCase.tasks.size() == 2
            assert useCase.tasks.any { it.transition == "person_info" }
            assert useCase.tasks.any { it.transition == "recreate_person" }
        }

        assert !migrationHelper.hasErrors()
    }

    @Test
    void migrationHelperShouldUpdatePetriNetAndApplyCustomTransitionRoleUpdate() {
        ProcessRole role = migrationHelper.createRoleInNet(
                "migration_test",
                "migration_supervisor",
                "Migration supervisor"
        )

        Closure<PetriNet> updateTransitionRole = migrationHelper.updateTransitionRolesClosure(
                "person_info",
                "migration_supervisor",
                [
                        view   : true,
                        perform: true
                ]
        )

        migrationHelper.updateNetIgnoreRoles("migration_test", "migration_test_v2.xml", [updateTransitionRole])

        PetriNet migratedNet = petriNetService.getDefaultVersionByIdentifier("migration_test")

        assert migratedNet.dataSet.containsKey("income")
        assert migratedNet.dataSet.containsKey("recreate_info_text")
        assert migratedNet.transitions.values().any { it.importId == "recreate_person" }

        ProcessRole migratedRole = migratedNet.roles.values().find {
            it.importId == "migration_supervisor"
        }
        assert migratedRole != null

        def personInfoTransition = migratedNet.transitions.values().find {
            it.importId == "person_info"
        }
        assert personInfoTransition != null
        assert personInfoTransition.roles[migratedRole.stringId]["view"]
        assert personInfoTransition.roles[migratedRole.stringId]["perform"]
    }

    @Test
    void migrationHelperShouldUpdateTasksAndAddRoleToExistingTasks() {
        ProcessRole role = migrationHelper.createRoleInNet(
                "migration_test",
                "migration_task_role",
                "Migration task role"
        )

        migrationHelper.addRoleToExistingTasks(
                role,
                netV1,
                ["person_info"],
                [
                        view   : true,
                        perform: true
                ]
        )

        Page<Case> casePage = workflowService.search(
                QCase.case$.processIdentifier.eq("migration_test"),
                Pageable.ofSize(10)
        )

        assert casePage.content.size() == 10

        casePage.content.each { Case useCase ->
            useCase.tasks.each { taskPair ->
                if (taskPair.transition == "person_info") {
                    Task task = taskService.findOne(taskPair.task)
                    assert task.roles.containsKey(role.stringId)
                    assert task.roles[role.stringId]["view"]
                    assert task.roles[role.stringId]["perform"]
                }
            }
        }

        migrationHelper.updateTasks(
                { Task task ->
                    task.title.defaultValue = "Migrated task"
                },
                QTask.task.transitionId.eq("person_info")
        )

        casePage.content.each { Case useCase ->
            useCase.tasks.each { taskPair ->
                if (taskPair.transition == "person_info") {
                    Task task = taskService.findOne(taskPair.task)
                    assert task.title.defaultValue == "Migrated task"
                }
            }
        }
    }

    @Test
    void migrationHelperShouldCollectErrorsAndContinueMigration() {
        migrationHelper.clearErrors()

        migrationHelper.withErrorPolicy(MigrationErrorPolicy.continueOnError()) {
            migrationHelper.updateAllCasesCursor({ Case useCase ->
                if (useCase.title.endsWith("1") || useCase.title.endsWith("2")) {
                    throw new IllegalStateException("Expected migration error for ${useCase.stringId}")
                }

                useCase.title = "Successfully migrated"
            }, 1)
        }

        assert migrationHelper.hasErrors()

        List<MigrationError> errors = migrationHelper.popErrors()
        assert errors.size() == 2
        assert errors.every { it.message.contains("Failed to prepare migration operation") }
        assert !migrationHelper.hasErrors()

        List<Case> cases = workflowService.search(
                QCase.case$.processIdentifier.eq("migration_test"),
                Pageable.ofSize(10)
        ).content

        assert cases.count { it.title == "Successfully migrated" } == 8
    }

    @Test
    void migrationHelperCollectErrorsShouldClearCacheBeforeAndAfterCollection() {
        migrationHelper.clearErrors()
        int allCases = workflowService.searchAll(QCase.case$._id.isNotNull()).getContent().size()

        List<MigrationError> errors = migrationHelper.collectErrors {
            migrationHelper.withErrorPolicy(MigrationErrorPolicy.continueOnError()) {
                migrationHelper.updateAllCasesCursor({ Case useCase ->
                    throw new IllegalStateException("Expected collected error")
                }, 1)
            }
        }

        assert errors.size() == allCases
        assert !migrationHelper.hasErrors()
        assert errors.every { it.cause instanceof IllegalStateException }
    }

    @Test
    void updateNetIgnoreRolesShouldMigrateExistingNet() {
        migrationHelper.updateNetIgnoreRoles("migration_test", "migration_test_v2.xml")

        def net = petriNetService.getDefaultVersionByIdentifier("migration_test")

        assert net.dataSet.containsKey("income")
        assert net.transitions.values().any { it.importId == "recreate_person" }
    }

    @Test
    void throwImmediately() {
        migrationHelper.clearErrors()

        assertThrows(MigrationErrorException) {
            migrationHelper.withErrorPolicy(MigrationErrorPolicy.throwImmediately()) {
                migrationHelper.updateAllCasesCursor({ Case useCase ->
                    throw new IllegalStateException("Expected test error")
                }, 1)
            }
        }

        assert migrationHelper.hasErrors()
    }

    @Test
    void throwAfterLimitIsReached() {
        migrationHelper.clearErrors()

        def exception = assertThrows(MigrationErrorException) {
            migrationHelper.withErrorPolicy(MigrationErrorPolicy.throwAfterLimit(2)) {
                migrationHelper.updateAllCasesCursor({ Case useCase ->
                    throw new IllegalStateException("Expected test error")
                }, 1)
            }
        }

        assert exception.errors.size() >= 2
    }

    @Test
    void throwAfterProcessingFinished() {
        migrationHelper.clearErrors()

        def exception = assertThrows(MigrationErrorException) {
            migrationHelper.withErrorPolicy(MigrationErrorPolicy.throwAfterProcessing()) {
                migrationHelper.updateAllCasesCursor({ Case useCase ->
                    throw new IllegalStateException("Expected test error")
                }, 1)
            }
        }

        assert exception.errors.size() > 0
    }

    @Test
    void continueOnError() {
        migrationHelper.clearErrors()

        migrationHelper.withErrorPolicy(MigrationErrorPolicy.continueOnError()) {
            migrationHelper.updateAllCasesCursor({ Case useCase ->
                throw new IllegalStateException("Expected test error")
            }, 1)
        }

        assert migrationHelper.hasErrors()
        assert migrationHelper.popErrors().size() > 0
    }
}
