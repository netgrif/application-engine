package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.workflow.service.interfaces.ICrudSystemCaseService;

import java.util.Set;

public interface IActorService<T extends Actor> extends ICrudSystemCaseService<T> {
    T addGroup(T actor, String groupId);
    T addGroups(T actor, Set<String> groupIdsToAdd);
    T removeGroup(T actor, String groupId);
    T removeGroups(T actor, Set<String> groupIdsToRemove);
    Group getDefaultGroup();
}
