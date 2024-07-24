package com.netgrif.application.engine.transaction;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.SuperCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
@ActiveProfiles({"test"})
public class TransactionTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    public void before() throws IOException, MissingPetriNetMetaDataException {
        testHelper.truncateDbs();
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/test_transaction.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper());
    }

    @Test
    public void testBasicTransaction() {
        // no multilevel
        // assert onCommit
        // consider onAlways
    }

    @Test
    public void testBasicTransactionWithFailure() {
        // no multilevel
        // assert onRollBack
        // consider onAlways
    }

    @Test
    public void testFailureInCallBackThrowsError() {
        // no multilevel
        // assert onRollBack
    }


    @Test
    public void testTimeout() {
        // assert if after some time the transaction is rolled back
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
