package com.netgrif.application.engine.transaction;

import groovy.lang.Closure;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.*;

import java.util.Date;

import static org.springframework.transaction.support.TransactionSynchronization.STATUS_COMMITTED;
import static org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK;

/**
 * todo
 * */
@Data
@Slf4j
@Builder
public class NaeTransaction {

    /**
     * todo
     * */
    private int timeout;
    private boolean forceCreation;
    private Closure<?> event;
    private Closure<?> onCommit;
    private Closure<?> onRollBack;

    private Exception onCallBackException;
    private Exception onEventException;
    private Propagation propagation;
    private Date deadline;
    private boolean wasRolledBack;

    /**
     * todo
     * */
    private final MongoTransactionManager transactionManager;

    /**
     * todo
     * */
    public void begin() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(propagation.value());
        setTimeoutInMillis(timeout);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    registerTransactionCallBacks();
                    event.call();
                    throwIfDeadlineReached(); // Transaction API does not check the timeout declared in transaction template
                } catch (Exception rethrow) {
                    onEventException = rethrow;
                    throw rethrow;
                }
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
                    wasRolledBack = true;
                    if (onEventException != null) {
                        onRollBack = onRollBack.curry(onEventException);
                    }
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
    private void setTimeoutInMillis(long millis) throws IllegalArgumentException {
        if (timeout == TransactionDefinition.TIMEOUT_DEFAULT) {
            return;
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException(String.format("Timeout can be %s or positive number to represent millis.",
                    TransactionDefinition.TIMEOUT_DEFAULT));
        }
        this.deadline = new Date(System.currentTimeMillis() + millis);
    }

    /**
     * todo
     */
    private void throwIfDeadlineReached() throws TransactionTimedOutException {
        if (this.deadline == null) {
            return;
        }
        long timeToLive = this.deadline.getTime() - System.currentTimeMillis();
        if (timeToLive <= 0) {
            throw new TransactionTimedOutException("Transaction timed out: deadline was " + this.deadline);
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
