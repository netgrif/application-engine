package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.repositories.IdentityRepository;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IdentityService implements IIdentityService {

    private final IdentityRepository repository;
    private final ISecurityContextService securityContextService;

    /**
     * todo javadoc
     */
    @Override
    public Optional<Identity> getLoggedIdentityFromContext() {
        // todo 2058 identitu ma aj system actor?
        Identity identity = (Identity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Optional.of(identity);
    }

    /**
     * todo javadoc
     */
    @Override
    public Optional<Identity> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        Optional<Identity> identityOpt = getLoggedIdentityFromContext();
        if (identityOpt.isPresent() && identityOpt.get().getId().equals(id)) {
            return identityOpt;
        }

        return repository.findById(id);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Optional<Identity> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }

        Optional<Identity> identityOpt = getLoggedIdentityFromContext();
        if (identityOpt.isPresent() && identityOpt.get().getUsername().equals(username)) {
            return identityOpt;
        }

        return repository.findByUsername(username);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Set<String> findActorIds(String id) {
        Optional<Identity> identityOpt = getLoggedIdentityFromContext();
        if (identityOpt.isEmpty() || !identityOpt.get().getId().equals(id)) {
            identityOpt = findById(id);
        }

        if (identityOpt.isPresent()) {
            Set<String> actorIds = new HashSet<>(identityOpt.get().getAdditionalActorIds());
            actorIds.add(identityOpt.get().getMainActorId());
            return actorIds;
        }

        return Set.of();
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity save(Identity identity) {
        if (identity == null) {
            return null;
        }

        identity = repository.save(identity);
        securityContextService.reloadSecurityContext(identity);

        return identity;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity addMainActor(String identityId, String actorId) {
        Optional<Identity> identityOpt = findById(identityId);
        if (identityOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not find identity with id [%s]", identityId));
        }

        return addMainActor(identityOpt.get(), actorId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity addMainActor(Identity identity, String actorId) {
        if (identity == null) {
            throw new IllegalArgumentException("Provided identity is null");
        }

        identity.setMainActorId(actorId);

        return save(identity);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity addAdditionalActor(String identityId, String actorId) {
        Optional<Identity> identityOpt = findById(identityId);
        if (identityOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not find identity with id [%s]", identityId));
        }
        return addAdditionalActors(identityOpt.get(), Set.of(actorId));
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
    public Identity addAdditionalActors(String identityId, Set<String> actorIds) {
        Optional<Identity> identityOpt = findById(identityId);
        if (identityOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not find identity with id [%s]", identityId));
        }
        return addAdditionalActors(identityOpt.get(), actorIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity addAdditionalActors(Identity identity, Set<String> actorIds) {
        if (identity == null) {
            throw new IllegalArgumentException("Provided identity is null");
        }

        identity.getAdditionalActorIds().addAll(actorIds);

        return save(identity);
    }
}
