package com.netgrif.application.engine.transaction;

import groovy.lang.Closure;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.*;

import static org.springframework.transaction.support.TransactionSynchronization.STATUS_COMMITTED;
import static org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK;

/**
 * todo
 * */
@Data
@Slf4j
@Builder
public class NaeTransaction {

    private int timeout;
    private boolean forceCreation;
    private Closure<?> event;
    private Closure<?> onCommit;
    private Closure<?> onRollBack;
    private Exception onCallBackException;
    private Propagation propagation;

    /**
     * todo
     * */
    private final MongoTransactionManager transactionManager;

    /**
     * todo
     * */
    public void begin() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setTimeout(timeout);
        transactionTemplate.setPropagationBehavior(propagation.value());

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                registerTransactionCallBacks();
                event.call();
            }
        });
    }

    /**
     * todo
     * does not log the error in the callback
     * */
    private void registerTransactionCallBacks() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            public void afterCompletion(int status) {
                if (canCallOnCommit(status)) {
                    runTransactionCallBack(onCommit);
                } else if (canCallOnRollBack(status)) {
                    runTransactionCallBack(onRollBack);
                }
            }
        });
    }

    private boolean canCallOnCommit(int status) {
        return status == STATUS_COMMITTED && onCommit != null;
    }

    private boolean canCallOnRollBack(int status) {
        return status == STATUS_ROLLED_BACK && onCommit != null;
    }

    /**
     * todo
     * */
    private void runTransactionCallBack(Closure<?> callBack) {
        try {
            callBack.run();
        } catch (Exception e) {
            // Transaction manager API swallows the exception
            onCallBackException = e;
            throw e;
        }
    }

    /**
     * todo
     * */
    public static class NaeTransactionBuilder {
        public NaeTransactionBuilder forceCreation(boolean forceCreation) {
            propagation = forceCreation ? Propagation.REQUIRES_NEW : Propagation.REQUIRED;
            this.forceCreation = forceCreation;
            return this;
        }
    }
}
