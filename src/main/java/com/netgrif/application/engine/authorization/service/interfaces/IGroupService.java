package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.Group;

import java.util.Optional;

public interface IGroupService extends IActorService<Group> {
    Optional<Group> findByName(String name);
    boolean existsByName(String name);
    Group getDefaultGroup();
}
