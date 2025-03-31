package com.netgrif.application.engine.orgstructure.groups.interfaces;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.querydsl.core.types.Predicate;

import java.util.*;

public interface INextGroupService {

    CreateCaseEventOutcome createDefaultSystemGroup(Actor author);

    CreateCaseEventOutcome createGroup(Actor author);

    CreateCaseEventOutcome createGroup(String title, Actor author);

    Case findGroup(String groupID);

    List<Case> findByIds(Collection<String> groupIds);

    List<Case> findAllGroups();

    Case findDefaultGroup();

    Case findByName(String name);

    List<Case> findByPredicate(Predicate predicate);

    Map<String, I18nString> inviteUser(String email, Map<String, I18nString> existingUsers, Case groupCase);

    void addUserToDefaultGroup(Actor actor);

    void addUser(Actor actor, String groupCase);

    void addUser(Actor actor, Case groupCase);

    Map<String, I18nString> addUser(Actor actor, Map<String, I18nString> existingUsers);

    void removeUser(Actor actor, Case groupCase);

    Map<String, I18nString> removeUser(HashSet<String> usersToRemove, Map<String, I18nString> existingUsers, Case groupCase);

    List<Actor> getMembers(Case groupCase);

    Set<String> getAllGroupsOfUser(Actor actor);

    String getGroupOwnerId(String groupId);

    Collection<String> getGroupsOwnerIds(Collection<String> groupIds);

    String getGroupOwnerEmail(String groupId);

    Collection<String> getGroupsOwnerEmails(Collection<String> groupIds);

    Set<String> getAllCoMembers(Actor actor);

}
