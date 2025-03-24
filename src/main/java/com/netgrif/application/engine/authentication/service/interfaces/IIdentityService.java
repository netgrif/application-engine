package com.netgrif.application.engine.authentication.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IIdentityService {
    LoggedIdentity getLoggedIdentity();
    LoggedIdentity getLoggedSystemIdentity();

    Optional<Identity> findById(String id);
    Optional<Identity> findByUsername(String username);
    Optional<Identity> findByLoggedIdentity(LoggedIdentity loggedIdentity);
    boolean existsByUsername(String username);
    Set<String> findActorIds(String id);
    List<Identity> findAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime);

    Identity create(IdentityParams params);
    Identity createWithDefaultActor(IdentityParams params);
    Identity encodePasswordAndCreate(IdentityParams params);
    Identity update(Identity identity, IdentityParams params);
    Identity encodePasswordAndUpdate(Identity identity, IdentityParams params);
    Identity addAdditionalActor(Identity identity, String actorId);
    Identity addAdditionalActors(Identity identity, Set<String> actorIds);

    List<Identity> removeAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime);
}
