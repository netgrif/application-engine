package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.security.service.SecurityContextService;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.service.CrudSystemCaseService;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IdentityService extends CrudSystemCaseService<Identity> implements IIdentityService {

    // todo: release/8.0.0 make encoder configurable
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityContextService securityContextService;
    private final IUserService userService;

    public IdentityService(BCryptPasswordEncoder passwordEncoder, SecurityContextService securityContextService,
                           @Lazy IDataService dataService, @Lazy IWorkflowService workflowService,
                           @Lazy IElasticCaseSearchService elasticCaseSearchService, @Lazy IUserService userService,
                           SystemCaseFactoryRegistry systemCaseFactoryRegistry, @Lazy ISessionManagerService sessionManagerService) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactoryRegistry, elasticCaseSearchService);
        this.passwordEncoder = passwordEncoder;
        this.securityContextService = securityContextService;
        this.userService = userService;
    }

    /**
     * Returns the currently logged identity from the session manager.
     *
     * @return Currently logged identity. Can be null if no identity is logged in.
     */
    @Override
    public LoggedIdentity getLoggedIdentity() {
        return sessionManagerService.getLoggedIdentity();
    }

    /**
     * Finds identity by username data field.
     *
     * @param username username of the identity. If provided null, empty optional is returned
     *
     * @return If the identity exists, it's returned. If not, an empty optional is returned
     * */
    @Override
    public Optional<Identity> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return findOneByQuery(fulltextFieldQuery(IdentityConstants.USERNAME_FIELD_ID, username));
    }

    /**
     * Finds identity by provided {@link LoggedIdentity}
     *
     * @param loggedIdentity logged identity used to find the identity. If provided null, empty optional is returned
     *
     * @return If the identity exists, it's returned. If not, an empty optional is returned
     * */
    @Override
    public Optional<Identity> findByLoggedIdentity(LoggedIdentity loggedIdentity) {
        if (loggedIdentity == null) {
            return Optional.empty();
        }

        Optional<Identity> identityOpt = findById(loggedIdentity.getIdentityId());
        if (identityOpt.isEmpty()) {
            log.warn("Logged identity [{}] has no identity in database!", loggedIdentity.getUsername());
        }
        return identityOpt;
    }

    /**
     * Checks if the identity exists by username data field
     *
     * @param username username of the identity. If provided null, empty optional is returned
     *
     * @return True if the identity exists, else false.
     * */
    @Override
    public boolean existsByUsername(String username) {
        if (username == null) {
            return false;
        }
        return countByQuery(fulltextFieldQuery(IdentityConstants.USERNAME_FIELD_ID, username)) > 0;
    }

    /**
     * Finds all actor ids of the identity
     *
     * @param id The id of the identity. If provided null, empty set is returned
     *
     * @return Set of actor ids of the identity. Otherwise, an empty set is returned
     * */
    @Override
    public Set<String> findActorIds(String id) {
        Optional<Identity> identityOpt = findById(id);

        if (identityOpt.isPresent()) {
            return identityOpt.get().getAllActors();
        }

        return Set.of();
    }

    /**
     * Find all identities with matched state, that are expired.
     *
     * @param state state of the identity. If provided null, empty list is returned
     * @param dateTime expiration date time threshold. The identity is considered expired if the expiration date is
     *                 before this value. If provided null, empty list is returned
     *
     * @return List of all identities, that match the requirements. Otherwise, an empty list is returned.
     * */
    @Override
    public List<Identity> findAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime) {
        if (state == null || dateTime == null) {
            return List.of();
        }
        return findAllByQuery(stateAndExpirationDateBeforeQuery(state, dateTime)).stream()
                .map(Identity::new)
                .collect(Collectors.toList());
    }

    /**
     * Creates identity based on params. Password is encoded. User is created from the identity parameters.
     *
     * @param identityParams Parameters, that are used to create the identity. At least username must be provided.
     *
     * @return Created identity with the User (as {@link Identity#getMainActorId()}). Cannot be null.
     * */
    @Override
    @Transactional
    public Identity createWithDefaultUser(IdentityParams identityParams) {
        validateAndFixCreateParams(identityParams);

        UserParams userParams = UserParams.fromIdentityParams(identityParams);
        User defaultUser = userService.create(userParams);

        identityParams.setMainActor(CaseField.withValue(List.of(defaultUser.getStringId())));
        return encodePasswordAndCreate(identityParams);
    }

    /**
     * Creates new identity with encoded password from provided parameters.
     *
     * @param params Parameters for creating identity. Password will be encoded before creation.
     * @return Created identity with encoded password
     * @throws IllegalArgumentException if parameters validation fails
     * */
    @Override
    @Transactional
    public Identity encodePasswordAndCreate(IdentityParams params) {
        encodePassword(params);
        return create(params);
    }

    /**
     * Updates existing identity with provided parameters and encodes new password.
     *
     * @param identity Identity to be updated
     * @param params Parameters containing updates. Password will be encoded before update.
     * @return Updated identity with newly encoded password
     * @throws IllegalArgumentException if identity or parameters validation fails
     * */
    @Override
    @Transactional
    public Identity encodePasswordAndUpdate(Identity identity, IdentityParams params) {
        encodePassword(params);
        return update(identity, params);
    }

    /**
     * Adds a single additional actor to the identity.
     *
     * @param identity Identity to which the actor will be added
     * @param actorId ID of the actor to be added as additional actor
     * @return Updated identity with new additional actor
     * @throws IllegalArgumentException if identity or actorId is null
     * */
    @Override
    @Transactional
    public Identity addAdditionalActor(Identity identity, String actorId) {
        if (identity == null) {
            throw new IllegalArgumentException("Provided identity is null");
        }
        if (actorId == null) {
            throw new IllegalArgumentException("Provided actorId is null");
        }

        return addAdditionalActors(identity, Set.of(actorId));
    }

    /**
     * Adds multiple additional actors to the identity.
     *
     * @param identity Identity to which the actors will be added
     * @param actorIds Set of actor IDs to be added as additional actors
     * @return Updated identity with new additional actors
     * @throws IllegalArgumentException if identity is null or actorIds is null or empty
     * */
    @Override
    @Transactional
    public Identity addAdditionalActors(Identity identity, Set<String> actorIds) {
        if (identity == null) {
            throw new IllegalArgumentException("Provided identity is null");
        }
        if (actorIds == null || actorIds.isEmpty()) {
            throw new IllegalArgumentException("Additional actors are not provided");
        }

        List<String> additionalActorIds;
        if (identity.getAdditionalActorIds() != null) {
            additionalActorIds = new ArrayList<>(identity.getAdditionalActorIds());
            additionalActorIds.addAll(actorIds);
        } else {
            additionalActorIds = new ArrayList<>(actorIds);
        }

        return update(identity, IdentityParams.with()
                .additionalActors(CaseField.withValue(additionalActorIds))
                .build());
    }

    /**
     * Removes all identities in given state that expired before specified date.
     *
     * @param state State of identities to be removed
     * @param dateTime Date threshold - identities expired before this date will be removed
     * @return List of all removed identities
     * @throws IllegalArgumentException if state or dateTime is null
     * */
    @Override
    @Transactional
    public List<Identity> removeAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime) {
        if (state == null || dateTime == null) {
            throw new IllegalArgumentException("Identity state or expiration date is null");
        }

        List<Identity> identities = findAllByStateAndExpirationDateBefore(state, dateTime);
        for (Identity identity : identities) {
            workflowService.deleteCase(identity.getCase());
        }

        return identities;
    }

    @Override
    protected String getProcessIdentifier() {
        return IdentityConstants.PROCESS_IDENTIFIER;
    }

    @Override
    protected String isUniqueQuery(CaseParams params) {
        IdentityParams typedParams = (IdentityParams) params;
        return fulltextFieldQuery(IdentityConstants.USERNAME_FIELD_ID, typedParams.getUsername().getRawValue());
    }

    @Override
    protected void validateAndFixCreateParams(CaseParams params) throws IllegalArgumentException {
        IdentityParams typedParams = (IdentityParams) params;
        if (isTextFieldOrValueEmpty(typedParams.getUsername())) {
            throw new IllegalArgumentException("Identity must have an username!");
        }
        if (isForbidden(typedParams.getUsername().getRawValue())) {
            throw new IllegalArgumentException(String.format("Identity username [%s] is reserved by the system.",
                    typedParams.getUsername().getRawValue()));
        }
    }

    @Override
    protected void validateAndFixUpdateParams(CaseParams params) throws IllegalArgumentException {
        IdentityParams typedParams = (IdentityParams) params;
        if (typedParams.getUsername() == null) {
            return;
        }
        if (isTextFieldValueEmpty(typedParams.getUsername())) {
            throw new IllegalArgumentException("Identity must have an username!");
        }
        if (isForbidden(typedParams.getUsername().getRawValue())) {
            throw new IllegalArgumentException(String.format("Identity username [%s] is reserved by the system.",
                    typedParams.getUsername().getRawValue()));
        }
    }

    @Override
    protected void postUpdateActions(Identity identity) {
        if (securityContextService.isIdentityLogged(identity.getStringId())) {
            securityContextService.reloadSecurityContext(identity.toSession());
        }
    }

    private void encodePassword(IdentityParams params) {
        String password = params.getPassword();
        if (password == null) {
            params.setPassword(new TextField(null));
            return;
        }
        params.setPassword(new TextField(passwordEncoder.encode(password)));
    }

    private static String stateAndExpirationDateBeforeQuery(IdentityState state, LocalDateTime dateTime) {
        long timestamp = Timestamp.valueOf(dateTime).getTime();
        return String.format("dataSet.%s.keyValue:\"%s\" AND dataSet.%s.timestampValue:<=%d",
                IdentityConstants.STATE_FIELD_ID, state.name().toLowerCase(), IdentityConstants.EXPIRATION_DATE_FIELD_ID,
                timestamp);
    }

}
