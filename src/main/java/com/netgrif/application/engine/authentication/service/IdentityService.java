package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
public class IdentityService implements IIdentityService {

    private final IElasticCaseService elasticCaseService;
    private final IWorkflowService workflowService;
    private final IDataService dataService;
    private final IUserService userService;
    // todo: release/8.0.0 make encoder configurable
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoggedIdentity getLoggedIdentity() {
        return (LoggedIdentity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * todo javadoc
     */
    @Override
    public Optional<Identity> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.of((Identity) workflowService.findOne(id));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * todo javadoc
     * */
    @Override
    public Optional<Identity> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return findOneByQuery(usernameQuery(username));
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) {
            return false;
        }
        return countByQuery(usernameQuery(username)) > 0;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Set<String> findActorIds(String id) {
        Optional<Identity> identityOpt = findById(id);

        if (identityOpt.isPresent()) {
            return identityOpt.get().getAllActors();
        }

        return Set.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Identity> findAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime) {
        if (state == null || dateTime == null) {
            return List.of();
        }
        return (List<Identity>) findAllByQuery(stateAndExpirationDateBeforeQuery(state, dateTime));
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity create(IdentityParams params) {
        Identity identity = (Identity) workflowService.createCaseByIdentifier(IdentityConstants.PROCESS_IDENTIFIER,
                params.getFullName(), "", getLoggedIdentity());
        return (Identity) dataService.setData(identity, params.toDataSet(), userService.getSystem()).getCase();
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
        return (Identity) dataService.setData(identity, params.toDataSet(), userService.getSystem()).getCase();
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

        List<String> additionalActorIds = new ArrayList<>(identity.getAdditionalActorIds());
        additionalActorIds.addAll(actorIds);

        return update(identity, IdentityParams.with()
                .additionalActors(CaseField.withValue(additionalActorIds))
                .build());
    }

    /**
     * todo javadoc
     * */
    @Override
    @SuppressWarnings("unchecked")
    public List<Identity> removeAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime) {
        if (state == null || dateTime == null) {
            return List.of();
        }

        List<Identity> identities = (List<Identity>) findAllByQuery(stateAndExpirationDateBeforeQuery(state, dateTime));
        for (Identity identity : identities) {
            workflowService.deleteCase(identity);
        }

        return identities;
    }

    private void encodePassword(IdentityParams params) {
        String password = params.getPassword();
        if (password == null) {
            throw new IllegalArgumentException("Identity has no password");
        }
        params.setPassword(new TextField(passwordEncoder.encode(password)));
    }

    private Optional<Identity> findOneByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        Page<Case> resultAsPage = elasticCaseService.search(List.of(request), getLoggedIdentity(), PageRequest.of(0, 1),
                Locale.getDefault(), false);
        if (resultAsPage.hasContent()) {
            return Optional.of((Identity) resultAsPage.getContent().get(0));
        }
        return Optional.empty();
    }

    private List<? extends Case> findAllByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();

        List<Case> result = new ArrayList<>();

        long identityCount = elasticCaseService.count(List.of(request), getLoggedIdentity(), Locale.getDefault(), false);
        long pageCount = (identityCount / 100) + 1;
        LongStream.range(0, pageCount).forEach(pageIdx -> {
            Page<Case> pageResult = elasticCaseService.search(List.of(request), getLoggedIdentity(), PageRequest.of((int) pageIdx, 100),
                    Locale.getDefault(), false);
            result.addAll(pageResult.getContent());
        });

        return result;
    }

    private long countByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        return elasticCaseService.count(List.of(request), getLoggedIdentity(), Locale.getDefault(), false);
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
                IdentityConstants.STATE_FIELD_ID, state.name(), IdentityConstants.EXPIRATION_DATE_FIELD_ID, timestamp);
    }

}
