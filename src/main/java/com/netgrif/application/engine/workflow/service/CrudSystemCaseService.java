package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import com.netgrif.application.engine.workflow.service.interfaces.ICrudSystemCaseService;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class CrudSystemCaseService<T extends SystemCase> implements ICrudSystemCaseService<T> {

    protected final ISessionManagerService sessionManagerService;
    protected final IDataService dataService;
    protected final IWorkflowService workflowService;
    protected final SystemCaseFactoryRegistry systemCaseFactory;

    // todo javadoc on abstract methods
    protected abstract String getProcessIdentifier();
    protected abstract void validateCreateParams(CaseParams params) throws IllegalArgumentException;
    protected abstract void validateUpdateParams(CaseParams params) throws IllegalArgumentException;
    protected abstract void postUpdateActions(SystemCase systemCase);

    /**
     * todo javadoc
     * Creates identity based on params. Password is not encoded. User is not created.
     *
     * @param params Parameters, that are used to create the identity. At least username must be provided.
     *
     * @return Created identity. Cannot be null
     *
     * @throws IllegalArgumentException if the input parameters are invalid
     * */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public T create(CaseParams params) throws IllegalArgumentException, IllegalStateException {
        validateCreateParams(params);

        final String activeActorId = sessionManagerService.getActiveActorId();
        Case systemCase = workflowService.createCaseByIdentifier(getProcessIdentifier(), null, "",
                activeActorId).getCase();
        T systemObject = (T) systemCaseFactory.fromCase(dataService.setData(systemCase, params.toDataSet(),
                activeActorId).getCase());

        if (systemObject == null) {
            throw new IllegalStateException(String.format("Unexpected: No wrapper class factory is registered for process [%s]",
                    getProcessIdentifier()));
        }

        log.debug("System case [{}][{}] was created by actor [{}].", systemCase, getProcessIdentifier(), activeActorId);
        return systemObject;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public T update(SystemCase systemObject, CaseParams params) throws IllegalArgumentException, IllegalStateException {
        if (systemObject == null) {
            throw new IllegalArgumentException("Please provide case to be updated");
        }

        validateUpdateParams(params);

        final String activeActorId = sessionManagerService.getActiveActorId();
        systemObject = systemCaseFactory.fromCase(dataService.setData(systemObject.getCase(), params.toDataSet(),
                        activeActorId).getCase());

        if (systemObject == null) {
            throw new IllegalStateException(String.format("Unexpected: No wrapper class factory is registered for process [%s]",
                    getProcessIdentifier()));
        }

        postUpdateActions(systemObject);

        log.debug("System case [{}][{}] was updated by actor [{}].", systemObject, getProcessIdentifier(), activeActorId);
        return (T) systemObject;
    }

    /**
     * todo javadoc
     * Finds identity by id.
     *
     * @param id id of the identity. If provided null, empty optional is returned
     *
     * @return If the identity exists, it's returned. If not, an empty optional is returned
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            final Case systemCase = workflowService.findOne(id);
            if (!systemCase.getProcessIdentifier().equals(getProcessIdentifier())) {
                return Optional.empty();
            }
            return (Optional<T>) Optional.ofNullable(systemCaseFactory.fromCase(systemCase));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        // todo: release/8.0.0 edge case: can return true and findById will return empty optional
        return workflowService.count(QCase.case$.processIdentifier.eq(getProcessIdentifier())
                .and(QCase.case$.id.eq(new ObjectId(id)))) > 0;
    }

    // todo: release/8.0.0 also removal method?

    protected boolean isTextFieldOrValueEmpty(TextField field) {
        return field == null || isTextFieldValueEmpty(field);
    }

    protected boolean isTextFieldValueEmpty(TextField field) {
        return field.getRawValue() == null || field.getRawValue().trim().isEmpty();
    }
}
