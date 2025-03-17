package com.netgrif.application.engine.authentication.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;

import java.util.Optional;
import java.util.Set;

public interface IIdentityService {
    Optional<Identity> getLoggedIdentityFromContext();

    Optional<Identity> findById(String id);
    Optional<Identity> findByUsername(String username);
    Set<String> findActorIds(String id);

    Identity save(Identity identity);
    Identity addMainActor(String identityId, String actorId);

    Identity addMainActor(Identity identity, String actorId);

    Identity addAdditionalActor(String identityId, String actorId);

    Identity addAdditionalActor(Identity identity, String actorId);

    Identity addAdditionalActors(String identityId, Set<String> actorIds);

    Identity addAdditionalActors(Identity identity, Set<String> actorIds);
}
