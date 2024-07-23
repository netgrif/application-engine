package com.netgrif.application.engine.transaction;

import groovy.lang.Closure;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.*;

/**
 * todo
 * */
@Data
@Builder
public class NaeTransaction {

    private int timeout;
    private boolean forceCreation;
    @SuppressWarnings("rawtypes")
    private Closure event;
    @SuppressWarnings("rawtypes")
    private Closure onCommit;
    @SuppressWarnings("rawtypes")
    private Closure onRollBack;

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

        if (forceCreation) {
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        } else {
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        }

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                registerTransactionCallBacks();
                event.call();
            }
        });
    }

    /**
     * todo
     * */
    private void registerTransactionCallBacks() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    onCommit.run();
                } else if (status == STATUS_ROLLED_BACK) {
                    onRollBack.run();
                }
            }
        });
    }
}
