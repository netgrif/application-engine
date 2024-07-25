package com.netgrif.application.engine.transaction

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.ButtonField
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import org.junit.jupiter.api.BeforeEach
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
    private SuperCreator superCreator

    private PetriNet testNet

    @BeforeEach
    void before() throws IOException, MissingPetriNetMetaDataException {
        testHelper.truncateDbs()
        testNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/transaction/transaction_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
    }

    private Case findByCaseTitle(String title) {
        Page<Case> caseAsPage = workflowService.search(QCase.case$.title.eq(title), PageRequest.of(0, 1))
        if (caseAsPage.totalElements > 0) {
            return caseAsPage.first()
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

    @Test
    void testBasicTransaction() {
        Case useCase = importHelper.createCase("test", testNet)
        useCase = dataService.setData(useCase, new DataSet(["testBasicTransaction": new ButtonField(rawValue: 1)]
                as Map<String, Field<?>>), superCreator.getSuperUser()).case

        assert findAllByIdentifier("transaction_test").size() == 4
        assert findByCaseTitle("onButton")
        assert findByCaseTitle("onCommit")
        assert findByCaseTitle("onAlways")
        assert !findByCaseTitle("onRollBack")
        assert useCase.getDataSet().get("text_without_action").getValue().getValue() == "xxx"
    }

    @Test
    void testBasicTransactionWithFailure() {
        Case useCase = importHelper.createCase("test", testNet)
        useCase = dataService.setData(useCase, new DataSet(["testBasicTransactionWithFailure": new ButtonField(rawValue: 1)]
                as Map<String, Field<?>>), superCreator.getSuperUser()).case

        assert findAllByIdentifier("transaction_test").size() == 3
        assert !findByCaseTitle("onButton")
        assert !findByCaseTitle("onCommit")
        assert findByCaseTitle("onAlways")
        assert findByCaseTitle("onRollBack")
        assert useCase.getDataSet().get("text_without_action").getValue().getValue() != "xxx"
    }

    @Test
    void testFailureInCallBackThrowsError() {
        Case useCase = importHelper.createCase("test", testNet)
        assertThrows(RuntimeException.class, {
            dataService.setData(useCase, new DataSet(["testFailureInCallBackThrowsError": new ButtonField(rawValue: 1)]
                    as Map<String, Field<?>>), superCreator.getSuperUser())
        })
        assert findAllByIdentifier("transaction_test").size() == 1
        assert !findByCaseTitle("onAlways")
    }

    @Test
    void testTimeout() {
        Case useCase = importHelper.createCase("test", testNet)
        dataService.setData(useCase, new DataSet(["testTimeout": new ButtonField(rawValue: 1)]
                as Map<String, Field<?>>), superCreator.getSuperUser())

        assert findAllByIdentifier("transaction_test").size() == 3
        assert !findByCaseTitle("onButton")
        assert !findByCaseTitle("onCommit")
        assert findByCaseTitle("onAlways")
        assert findByCaseTitle("onRollBack")
    }

    @Test
    void testElasticIndexingOnTransactionFailure() {
        // assert if after failure exists case from transaction in elastic (it shouldnt)
    }

    @Test
    public void testTransactionRaceCondition() {
        // assert failure in one of two concurrent transactions
    }

    @Test
    public void testNestedJoinedTransactions() {
        // assert if all happy paths are executed
        // consider onAlways
    }

    @Test
    public void testNestedDifferentTransactions() {
        // assert if all happy paths are executed
        // consider onAlways
    }

    @Test
    public void testNestedJoinedTransactionsWithFailureInParentTransaction() {
        // assert if runtime exception causes all onRollBacks
        // consider onAlways
    }

    @Test
    public void testNestedJoinedTransactionsWithFailureInNestedTransaction() {
        // assert if runtime exception causes all onRollBacks
        // consider onAlways
    }

    @Test
    public void testNestedDifferentTransactionsWithFailureInParentTransaction() {
        // assert if runtime exception (in parent action) causes only relevant onRollBacks
        // consider onAlways

        // assert if runtime exception (in action where definition of different transition is) causes only relevant onRollBacks
        // consider onAlways
    }

    @Test
    public void testNestedDifferentTransactionsWithFailureInNestedTransaction() {
        // assert if runtime exception causes only relevant onRollBacks
        // consider onAlways
    }

}
