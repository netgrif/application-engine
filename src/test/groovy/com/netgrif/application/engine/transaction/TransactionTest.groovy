package com.netgrif.application.engine.transaction

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.ButtonField
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles

import static org.junit.jupiter.api.Assertions.assertThrows

@SpringBootTest
@ActiveProfiles(["test"])
class TransactionTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IDataService dataService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IElasticPetriNetService elasticPetriNetService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private IElasticTaskService elasticTaskService

    @Autowired
    private SuperCreator superCreator

    private PetriNet testNet

    @BeforeEach
    void before() throws IOException, MissingPetriNetMetaDataException {
        testHelper.truncateDbs()
        testNet = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/petriNets/transaction/transaction_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper())).getNet()
    }

    @Test
    void testBasicTransaction() {
        Case useCase = importHelper.createCase("test", testNet)
        useCase = dataService.setData(new SetDataParams(useCase, new DataSet(["testBasicTransaction": new ButtonField(rawValue: 1)]
                as Map<String, Field<?>>), superCreator.getSuperUser())).case

        assert findAllByIdentifier("transaction_test").size() == 4

        assert findCaseByTitle("onButton")
        assert findTaskByCaseTitle("onButton")

        assert findCaseByTitle("onCommit")
        assert findTaskByCaseTitle("onCommit")

        assert findCaseByTitle("onAlways")
        assert findTaskByCaseTitle("onAlways")

        assert !findCaseByTitle("onRollBack")
        assert !findTaskByCaseTitle("onRollBack")

        assert useCase.getDataSet().get("text_without_action").getValue().getValue() == "xxx"
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == false
    }

    @Test
    void testBasicTransactionWithFailure() {
        Case useCase = createTestCaseAndSetButton("test", "testBasicTransactionWithFailure")
        importHelper.createCase("toBeRemoved", testNet)

        assert findAllByIdentifier("transaction_test").size() == 4

        assert !findCaseByTitle("onButton")
        assert !findTaskByCaseTitle("onButton")

        assert !findCaseByTitle("onCommit")
        assert !findTaskByCaseTitle("onCommit")

        assert findCaseByTitle("onAlways")
        assert findTaskByCaseTitle("onAlways")

        assert findCaseByTitle("onRollBack")
        assert findTaskByCaseTitle("onRollBack")

        assert findCaseByTitle("toBeRemoved")
        assert findTaskByCaseTitle("toBeRemoved")

        assert useCase.getDataSet().get("text_without_action").getValue().getValue() != "xxx"
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == true
    }

    @Test
    void testFailureInCallBackThrowsError() {
        assertThrows(RuntimeException.class, {
            createTestCaseAndSetButton("test", "testFailureInCallBackThrowsError")
        })
        assert findAllByIdentifier("transaction_test").size() == 1
        assert !findCaseByTitle("onAlways")
        assert !findTaskByCaseTitle("onAlways")
    }

    @Test
    void testTimeout() {
        Case useCase = createTestCaseAndSetButton("test", "testTimeout")

        assert findAllByIdentifier("transaction_test").size() == 3

        assert !findCaseByTitle("onButton")
        assert !findTaskByCaseTitle("onButton")

        assert !findCaseByTitle("onCommit")
        assert !findTaskByCaseTitle("onCommit")

        assert findCaseByTitle("onAlways")
        assert findTaskByCaseTitle("onAlways")

        assert findCaseByTitle("onRollBack")
        assert findTaskByCaseTitle("onRollBack")

        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == true
    }

    @Test
    void testElasticIndexingOnTransactionFailure() {
        Case useCase = createTestCaseAndSetButton("test", "testElasticIndexingOnTransactionFailure")

        Thread.sleep(2000) // wait for indexation

        assert findAllByIdentifier("transaction_test").size() == 3

        assert !existPetriNetInElastic("transaction_test_secondary")
        assert !existCaseInElastic("toBeRemoved")
        assert !existCaseInElastic("toBeRemovedNestedOnFailure")

        assert !existCaseInElastic("onButton")
        assert !existTaskInElastic("onButton")

        assert existCaseInElastic("onRollBack")
        assert existTaskInElastic("onRollBack")

        assert existCaseInElastic("onAlways")
        assert existTaskInElastic("onAlways")

        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == true
    }

    @Test
    void testElasticIndexingOnTransactionSuccess() {
        Case useCase = createTestCaseAndSetButton("test", "testElasticIndexingOnTransactionSuccess")

        Thread.sleep(2000) // wait for indexation

        assert findAllByIdentifier("transaction_test").size() == 5

        assert existPetriNetInElastic("transaction_test_secondary")
        assert !existCaseInElastic("toBeRemoved")
        assert !existTaskInElastic("toBeRemoved")
        assert existCaseInElastic("toBeRemovedNestedOnFailure")
        assert existTaskInElastic("toBeRemovedNestedOnFailure")

        assert existCaseInElastic("onButton")
        assert existTaskInElastic("onButton")

        assert existCaseInElastic("onCommit")
        assert existTaskInElastic("onCommit")

        assert existCaseInElastic("onAlways")
        assert existTaskInElastic("onAlways")

        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == false
    }

    @Test
    void testTransactionWriteConflict() {
        Case useCase = createTestCaseAndSetButton("test", "testTransactionWriteConflict")

        assert findAllByIdentifier("transaction_test").size() == 5

        assert findCaseByTitle("onRollBackNested")
        assert findTaskByCaseTitle("onRollBackNested")

        assert !findCaseByTitle("onCommitNested")
        assert !findTaskByCaseTitle("onCommitNested")

        assert findCaseByTitle("onAlwaysNested")
        assert findTaskByCaseTitle("onAlwaysNested")

        assert findCaseByTitle("onCommit")
        assert findTaskByCaseTitle("onCommit")

        assert findCaseByTitle("onAlways")
        assert findTaskByCaseTitle("onAlways")

        useCase = workflowService.findOne(useCase.stringId)
        assert useCase.getDataSet().get("text_without_action").getValue().getValue() == "not nested"
    }

    @Test
    void testNestedJoinedTransactions() {
        Case useCase = createTestCaseAndSetButton("test", "testNestedJoinedTransactions")

        assert findAllByIdentifier("transaction_test").size() == 7
        assert findCaseByTitle("onButton")
        assert findCaseByTitle("onCommit")
        assert findCaseByTitle("onAlways")
        assert findCaseByTitle("onButtonNested")
        assert findCaseByTitle("onCommitNested")
        assert findCaseByTitle("onAlwaysNested")
        assert !findCaseByTitle("onRollBack")
        assert !findCaseByTitle("onRollBackNested")
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == false
    }

    @Test
    void testNestedDifferentTransactions() {
        Case useCase = createTestCaseAndSetButton("test", "testNestedDifferentTransactions")

        assert findAllByIdentifier("transaction_test").size() == 7
        assert findCaseByTitle("onButton")
        assert findCaseByTitle("onCommit")
        assert findCaseByTitle("onAlways")
        assert findCaseByTitle("onButtonNested")
        assert findCaseByTitle("onCommitNested")
        assert findCaseByTitle("onAlwaysNested")
        assert !findCaseByTitle("onRollBack")
        assert !findCaseByTitle("onRollBackNested")
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == false
    }

    @Test
    void testNestedJoinedTransactionsWithFailureInParentTransaction() {
        Case useCase = createTestCaseAndSetButton("test", "testNestedJoinedTransactionsWithFailureInParentTransaction")

        assert findAllByIdentifier("transaction_test").size() == 4
        assert !findCaseByTitle("onButton")
        assert !findCaseByTitle("onCommit")
        assert findCaseByTitle("onAlways")
        assert !findCaseByTitle("onButtonNested")
        assert !findCaseByTitle("onCommitNested")
        assert !findCaseByTitle("onAlwaysNested")
        assert findCaseByTitle("onRollBack")
        assert findCaseByTitle("onRollBackNested")
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == true
    }

    @Test
    void testNestedJoinedTransactionsWithFailureInNestedTransaction() {
        Case useCase = createTestCaseAndSetButton("test", "testNestedJoinedTransactionsWithFailureInNestedTransaction")

        assert findAllByIdentifier("transaction_test").size() == 4
        assert !findCaseByTitle("onButton")
        assert !findCaseByTitle("onCommit")
        assert findCaseByTitle("onAlways")
        assert !findCaseByTitle("onButtonNested")
        assert !findCaseByTitle("onCommitNested")
        assert !findCaseByTitle("onAlwaysNested")
        assert findCaseByTitle("onRollBack")
        assert findCaseByTitle("onRollBackNested")
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == true
    }

    @Test
    void testNestedDifferentTransactionsWithFailureInParentTransaction() {
        Case useCase = createTestCaseAndSetButton("test", "testNestedDifferentTransactionsWithFailureInParentTransaction")

        assert findAllByIdentifier("transaction_test").size() == 5
        assert !findCaseByTitle("onButton")
        assert !findCaseByTitle("onCommit")
        assert findCaseByTitle("onAlways")
        assert findCaseByTitle("onButtonNested")
        assert findCaseByTitle("onCommitNested")
        assert !findCaseByTitle("onAlwaysNested")
        assert findCaseByTitle("onRollBack")
        assert !findCaseByTitle("onRollBackNested")
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == true // is overridden from false to true
    }

    @Test
    void testNestedDifferentTransactionsWithFailureInNestedTransaction() {
        Case useCase = createTestCaseAndSetButton("test", "testNestedDifferentTransactionsWithFailureInNestedTransaction")

        assert findAllByIdentifier("transaction_test").size() == 6
        assert findCaseByTitle("onButton")
        assert findCaseByTitle("onCommit")
        assert findCaseByTitle("onAlways")
        assert !findCaseByTitle("onButtonNested")
        assert !findCaseByTitle("onCommitNested")
        assert findCaseByTitle("onAlwaysNested")
        assert !findCaseByTitle("onRollBack")
        assert findCaseByTitle("onRollBackNested")
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == false // is overridden from true to false
    }

    @Test
    void testRollBackErrorCurrying() {
        Case useCase = createTestCaseAndSetButton("test", "testRollBackErrorCurrying")

        assert findAllByIdentifier("transaction_test").size() == 3
        assert !findCaseByTitle("onButton")
        assert !findCaseByTitle("onCommit")
        assert findCaseByTitle("onAlways")
        assert findCaseByTitle("argument is initialized")
        assert useCase.getDataSet().get("was_transaction_rolled_back").getValue().getValue() == true
    }

    @Test
    @Disabled
    void testPerformance() {
        int iterations = 1000
        Case useCase = importHelper.createCase("performance test case", testNet)

        long totalTransactionalDuration = 0
        (0..iterations).each {
            Date startTime = new Date()
            dataService.setData(new SetDataParams(useCase, new DataSet(["testCreateCaseInTransactionPerformance": new ButtonField(rawValue: 1)]
                    as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
            Date endTime = new Date()
            TimeDuration elapsedTimeTransactional = TimeCategory.minus( endTime, startTime )
            totalTransactionalDuration += elapsedTimeTransactional.toMilliseconds()
        }

        long totalNonTransactionalDuration = 0
        (0..iterations).each {
            Date startTime = new Date()
            dataService.setData(new SetDataParams(useCase, new DataSet(["testCreateCasePerformance": new ButtonField(rawValue: 1)]
                    as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
            Date endTime = new Date()
            TimeDuration elapsedTimeTransactional = TimeCategory.minus( endTime, startTime )
            totalNonTransactionalDuration += elapsedTimeTransactional.toMilliseconds()
        }

        println("AVG transactional for 1 create case: " + totalTransactionalDuration / iterations + "ms")
        println("AVG non-transactional for 1 create case: " + totalNonTransactionalDuration / iterations + "ms")
    }

    private Case createTestCaseAndSetButton(String title, String buttonFieldId) {
        Case useCase = importHelper.createCase(title, testNet)
        return dataService.setData(new SetDataParams(useCase, new DataSet([(buttonFieldId): new ButtonField(rawValue: 1)]
                as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
    }

    private Case findCaseByTitle(String title) {
        Page<Case> caseAsPage = workflowService.search(QCase.case$.title.eq(title), PageRequest.of(0, 1))
        if (caseAsPage.totalElements > 0) {
            return caseAsPage.first()
        } else {
            return null
        }
    }

    private Task findTaskByCaseTitle(String caseTitle) {
        Page<Task> taskAsPage = taskService.search(QTask.task.caseTitle.eq(caseTitle), PageRequest.of(0, 1))
        if (taskAsPage.totalElements > 0) {
            return taskAsPage.first()
        } else {
            return null
        }
    }

    private List<Case> findAllByIdentifier(String identifier) {
        Page<Case> caseAsPage = workflowService.search(QCase.case$.processIdentifier.eq(identifier), PageRequest.of(0, Integer.MAX_VALUE))
        if (caseAsPage.totalElements > 0) {
            return caseAsPage.getContent()
        } else {
            return List.of()
        }
    }

    private boolean existPetriNetInElastic(String identifier) {
        return elasticPetriNetService.findAllByIdentifier(identifier).size() >= 1
    }

    private boolean existCaseInElastic(String title) {
        return elasticCaseService.count(
                List.of(CaseSearchRequest.builder().query("title:" + title).build()),
                superCreator.getLoggedSuper(),
                Locale.getDefault(),
                false
        ) >= 1
    }

    private boolean existTaskInElastic(String caseTitle) {
        return elasticTaskService.count(
                List.of(new ElasticTaskSearchRequest(Map.of("query", "caseTitle:" + caseTitle))),
                superCreator.getLoggedSuper(),
                Locale.getDefault(),
                false
        ) >= 1
    }

}
