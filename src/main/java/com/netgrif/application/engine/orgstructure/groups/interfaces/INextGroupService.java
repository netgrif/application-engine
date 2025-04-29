package com.netgrif.application.engine.orgstructure.groups.interfaces;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.querydsl.core.types.Predicate;

import java.util.*;

public interface INextGroupService {

    CreateCaseEventOutcome createDefaultSystemGroup(User author);

    CreateCaseEventOutcome createGroup(User author);

    CreateCaseEventOutcome createGroup(String title, User author);

    Case findGroup(String groupID);

    List<Case> findByIds(Collection<String> groupIds);

    List<Case> findAllGroups();

    Case findDefaultGroup();

    Case findByName(String name);

    List<Case> findByPredicate(Predicate predicate);

    Map<String, I18nString> inviteUser(String email, Map<String, I18nString> existingUsers, Case groupCase);

    void addUserToDefaultGroup(User actor);

    void addUser(User actor, String groupCase);

    void addUser(User actor, Case groupCase);

    Map<String, I18nString> addUser(User actor, Map<String, I18nString> existingUsers);

    void removeUser(User actor, Case groupCase);

    Map<String, I18nString> removeUser(HashSet<String> usersToRemove, Map<String, I18nString> existingUsers, Case groupCase);

    List<User> getMembers(Case groupCase);

    Set<String> getAllGroupsOfUser(User actor);

    String getGroupOwnerId(String groupId);

    Collection<String> getGroupsOwnerIds(Collection<String> groupIds);

    String getGroupOwnerEmail(String groupId);

    Collection<String> getGroupsOwnerEmails(Collection<String> groupIds);

    Set<String> getAllCoMembers(User actor);

}
