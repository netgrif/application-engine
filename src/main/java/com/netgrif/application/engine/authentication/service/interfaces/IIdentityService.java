package com.netgrif.application.engine.authentication.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.workflow.service.interfaces.ICrudSystemCaseService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IIdentityService extends ICrudSystemCaseService<Identity> {
    Optional<Identity> findByUsername(String username);
    Optional<Identity> findByLoggedIdentity(LoggedIdentity loggedIdentity);
    boolean existsByUsername(String username);
    Set<String> findActorIds(String id);
    List<Identity> findAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime);
    List<Identity> findAll();

    Identity createWithDefaultUser(IdentityParams params);
    Identity encodePasswordAndCreate(IdentityParams params);
    Identity encodePasswordAndUpdate(Identity identity, IdentityParams params);
    Identity addAdditionalActor(Identity identity, String actorId);
    Identity addAdditionalActors(Identity identity, Set<String> actorIds);

    List<Identity> removeAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime);
}
