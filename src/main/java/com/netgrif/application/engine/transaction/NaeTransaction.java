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
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate;

import java.util.Date;

import static org.springframework.transaction.support.TransactionSynchronization.STATUS_COMMITTED;
import static org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK;


@Data
@Slf4j
@Builder
public class NaeTransaction {

    /**
     * Timeout for the transaction in milliseconds. When the timeout is reached, transaction fails on commit.
     * */
    @Builder.Default
    private int timeout = TransactionDefinition.TIMEOUT_DEFAULT;
    /**
     * If set to true, new transaction is created in any situation (uses {@link Propagation#REQUIRES_NEW}). If set to
     * false, transaction is created only if none exists (uses {@link Propagation#REQUIRED}).
     * */
    @Builder.Default
    private boolean forceCreation = false;
    /**
     * Transaction code to be executed under transaction.
     * */
    private Closure<?> event;
    /**
     * Return value of the {@link #event}
     * */
    private Object resultOfEvent;
    /**
     * Callback, that is called when {@link NaeTransaction#event} is successful.
     * */
    private Closure<?> onCommit;
    /**
     * Callback, that is called when {@link NaeTransaction#event} fails. The closure can contain input attribute. If so,
     * the input attribute is initialized by the subject exception ({@link NaeTransaction#onEventException})
     * */
    private Closure<?> onRollBack;

    private Exception onCallBackException;
    private Exception onEventException;
    @Builder.Default
    private Propagation propagation = Propagation.REQUIRED;
    private Date deadline;
    private boolean wasRolledBack;

    /**
     * Singleton bean of Mongo transaction manager provided from {@link ActionDelegate#getTransactionManager}
     * */
    private final MongoTransactionManager transactionManager;

    /**
     * Does additional setups for transaction by {@link TransactionTemplate} and executes provided {@link NaeTransaction#event}.
     * <br>
     * If the execution is successful callback {@link NaeTransaction#onCommit} is called. Otherwise {@link NaeTransaction#onRollBack}
     * is called. If any of the callback fails, exception isn't thrown, but saved in {@link NaeTransaction#onCallBackException}
     * */
    public void begin() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(propagation.value());
        setTimeoutInMillis(timeout);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    registerTransactionCallBacks();
                    resultOfEvent = event.call();
                    throwIfDeadlineReached(); // Transaction API does not check the timeout declared in transaction template
                } catch (Exception rethrow) {
                    onEventException = rethrow;
                    throw rethrow;
                }
            }
        });
    }

    /**
     * Registers callbacks for the active transaction. Must be called under active transaction. Callbacks are registered
     * by {@link TransactionSynchronizationManager#registerSynchronization(TransactionSynchronization)}. If any of the
     * callbacks fails the exception is not thrown. It's saved in {@link NaeTransaction#onCallBackException}.
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
        return status == STATUS_ROLLED_BACK && onRollBack != null;
    }

    /**
     * Runs the provided callback. If the callback fails, the exception is saved in {@link NaeTransaction#onCallBackException}
     * and thrown.
     *
     * @param callBack callback to be executed
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
     * Initializes the {@link NaeTransaction#deadline} field by the provided timeout in milliseconds.
     *
     * @param millis timeout in milliseconds. Can be {@link TransactionDefinition#TIMEOUT_DEFAULT} or positive number
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
     * Throws the {@link TransactionTimedOutException} if the {@link NaeTransaction#deadline} is reached
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
     * Builder extension of the {@link Builder} implementation for {@link }. Containing additional logic over the native builder
     * implementation
     * */
    public static class NaeTransactionBuilder {
        public NaeTransactionBuilder forceCreation(boolean forceCreation) {
            this.propagation$value = forceCreation ? Propagation.REQUIRES_NEW : Propagation.REQUIRED;
            this.forceCreation$value = forceCreation;
            return this;
        }
    }
}
