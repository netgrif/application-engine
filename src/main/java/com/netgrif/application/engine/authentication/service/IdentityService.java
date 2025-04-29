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
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.security.service.SecurityContextService;
import com.netgrif.application.engine.startup.SystemIdentityRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
public class IdentityService implements IIdentityService {

    // todo: release/8.0.0 make encoder configurable
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityContextService securityContextService;
    private final SystemIdentityRunner systemIdentityRunner;
    private final IDataService dataService;
    private final IWorkflowService workflowService;
    private final IElasticCaseSearchService elasticCaseSearchService;
    private final IUserService userService;

    public IdentityService(BCryptPasswordEncoder passwordEncoder, SecurityContextService securityContextService,
                           @Lazy SystemIdentityRunner systemIdentityRunner, @Lazy IDataService dataService,
                           @Lazy IWorkflowService workflowService, @Lazy IElasticCaseSearchService elasticCaseSearchService,
                           @Lazy IUserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.securityContextService = securityContextService;
        this.systemIdentityRunner = systemIdentityRunner;
        this.dataService = dataService;
        this.workflowService = workflowService;
        this.elasticCaseSearchService = elasticCaseSearchService;
        this.userService = userService;
    }

    /**
     * Gets currently logged identity
     *
     * @return Currently logged identity. Can be null if nobody is logged in.
     */
    @Override
    public LoggedIdentity getLoggedIdentity() {
        if (securityContextService.isAuthenticatedPrincipalLoggedIdentity()) {
            return (LoggedIdentity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        return null;
    }

    /**
     * Gets logged system identity. However, this identity is not managed by session manager.
     *
     * @return Logged system identity. Cannot be null.
     */
    @Override
    public LoggedIdentity getLoggedSystemIdentity() {
        return systemIdentityRunner.getLoggedSystem();
    }

    /**
     * Gets id of currently selected actor of logged identity
     *
     * @return The id of the selected actor if any identity is logged in. Can be null.
     */
    @Override
    public String getActiveActorId() {
        LoggedIdentity loggedIdentity = getLoggedIdentity();
        if (loggedIdentity != null) {
            return loggedIdentity.getActiveActorId();
        }
        return null;
    }

    /**
     * Finds identity by id.
     *
     * @param id id of the identity. If provided null, empty optional is returned
     *
     * @return If the identity exists, it's returned. If not, an empty optional is returned
     */
    @Override
    public Optional<Identity> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Identity(workflowService.findOne(id)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
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
        return findOneByQuery(usernameQuery(username));
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
        return countByQuery(usernameQuery(username)) > 0;
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
     * Finds all identities
     *
     * @return List of all identities. Cannot be null
     * */
    @Override
    public List<Identity> findAll() {
        return findAllByQuery(null).stream().map(Identity::new).collect(Collectors.toList());
    }

    /**
     * Creates identity based on params. Password is not encoded. Actor is not created.
     *
     * @param params Parameters, that are used to create the identity. At least username must be provided.
     *
     * @return Created identity. Cannot be null
     *
     * @throws IllegalArgumentException if the input parameters are invalid
     * */
    @Override
    public Identity create(IdentityParams params) {
        throwIfInvalidParams(params);

        String activeActorId = getActiveActorId();
        Case identityCase = workflowService.createCaseByIdentifier(IdentityConstants.PROCESS_IDENTIFIER,
                params.getFullName(), "", activeActorId).getCase();
        Identity identity = new Identity(dataService.setData(identityCase, params.toDataSet(), activeActorId).getCase());
        log.debug("Identity [{}][{}] was created by actor [{}].", identity.getStringId(), identity.getFullName(), activeActorId);
        return identity;
    }

    /**
     * Creates identity based on params. Password is encoded. Actor is created from the identity parameters.
     *
     * @param identityParams Parameters, that are used to create the identity. At least username must be provided.
     *
     * @return Created identity with the actor (as {@link Identity#getMainActorId()}). Cannot be null.
     * */
    @Override
    public Identity createWithDefaultActor(IdentityParams identityParams) {
        throwIfInvalidParams(identityParams);

        UserParams userParams = UserParams.fromIdentityParams(identityParams);
        User defaultUser = userService.create(userParams);

        identityParams.setMainActor(CaseField.withValue(List.of(defaultUser.getStringId())));
        return encodePasswordAndCreate(identityParams);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity encodePasswordAndCreate(IdentityParams params) {
        encodePassword(params);
        return create(params);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity update(Identity identity, IdentityParams params) {
        if (params == null || (params.getUsername() != null && isTextFieldValueEmpty(params.getUsername()))) {
            throw new IllegalArgumentException("Identity must have an username!");
        }
        if (identity == null) {
            throw new IllegalArgumentException("Please provide identity to be updated");
        }

        String activeActorId = getActiveActorId();
        identity = new Identity(dataService.setData(identity.getCase(), params.toDataSet(), activeActorId)
                .getCase());
        if (securityContextService.isIdentityLogged(identity.getStringId())) {
            securityContextService.reloadSecurityContext(identity.toSession());
        }
        return identity;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity encodePasswordAndUpdate(Identity identity, IdentityParams params) {
        encodePassword(params);
        return update(identity, params);
    }

    /**
     * todo javadoc
     * */
    @Override
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
     * todo javadoc
     * */
    @Override
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
     * todo javadoc
     * */
    @Override
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

    private void throwIfInvalidParams(IdentityParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for actor");
        }
        if (isTextFieldOrValueEmpty(params.getUsername())) {
            throw new IllegalArgumentException("Identity must have an username!");
        }
    }

    private boolean isTextFieldOrValueEmpty(TextField field) {
        return field == null || isTextFieldValueEmpty(field);
    }

    private boolean isTextFieldValueEmpty(TextField field) {
        return field.getRawValue() == null || field.getRawValue().trim().isEmpty();
    }

    private void encodePassword(IdentityParams params) {
        String password = params.getPassword();
        if (password == null) {
            params.setPassword(new TextField(null));
            return;
        }
        params.setPassword(new TextField(passwordEncoder.encode(password)));
    }

    private Optional<Identity> findOneByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        Page<Case> resultAsPage = elasticCaseSearchService.search(List.of(request), getLoggedIdentity(), PageRequest.of(0, 1),
                Locale.getDefault(), false, null);
        if (resultAsPage.hasContent()) {
            return Optional.of(new Identity(resultAsPage.getContent().get(0)));
        }
        return Optional.empty();
    }

    private List<? extends Case> findAllByQuery(String query) {
        Set<String> singletonQuerySet = query != null ? Set.of(query) : Set.of();
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(singletonQuerySet))
                .build();

        List<Case> result = new ArrayList<>();

        long identityCount = elasticCaseSearchService.count(List.of(request), getLoggedIdentity(), Locale.getDefault(),
                false, null);
        long pageCount = (identityCount / 100) + 1;
        LongStream.range(0, pageCount).forEach(pageIdx -> {
            Page<Case> pageResult = elasticCaseSearchService.search(List.of(request), getLoggedIdentity(), PageRequest.of((int) pageIdx, 100),
                    Locale.getDefault(), false, null);
            result.addAll(pageResult.getContent());
        });

        return result;
    }

    private long countByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        return elasticCaseSearchService.count(List.of(request), getLoggedIdentity(), Locale.getDefault(),
                false, null);
    }

    private static String buildQuery(Set<String> andQueries) {
        StringBuilder queryBuilder = new StringBuilder("processIdentifier:identity");
        for (String query : andQueries) {
            queryBuilder.append(" AND ");
            queryBuilder.append(query);
        }
        return queryBuilder.toString();
    }

    private static String usernameQuery(String username) {
        return String.format("dataSet.%s.fulltextValue:\"%s\"", IdentityConstants.USERNAME_FIELD_ID, username);
    }

    private static String stateAndExpirationDateBeforeQuery(IdentityState state, LocalDateTime dateTime) {
        long timestamp = Timestamp.valueOf(dateTime).getTime();
        return String.format("dataSet.%s.keyValue:\"%s\" AND dataSet.%s.timestampValue:<=%d",
                IdentityConstants.STATE_FIELD_ID, state.name().toLowerCase(), IdentityConstants.EXPIRATION_DATE_FIELD_ID,
                timestamp);
    }

}
