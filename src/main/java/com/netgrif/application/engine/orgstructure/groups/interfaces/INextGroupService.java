package com.netgrif.application.engine.orgstructure.groups.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.querydsl.core.types.Predicate;

import java.util.*;

public interface INextGroupService {

    CreateCaseEventOutcome createDefaultSystemGroup(IUser author);

    CreateCaseEventOutcome createGroup(IUser author);

    CreateCaseEventOutcome createGroup(String title, IUser author);

    Case findGroup(String groupID);

    List<Case> findByIds(Collection<String> groupIds);

    List<Case> findAllGroups();

    Case findDefaultGroup();

    Case findByName(String name);

    List<Case> findByPredicate(Predicate predicate);

    Map<String, I18nString> inviteUser(String email, Map<String, I18nString> existingUsers, Case groupCase);

    void addUserToDefaultGroup(IUser user);

    void addUser(IUser user, String groupCase);

    void addUser(IUser user, Case groupCase);

    Map<String, I18nString> addUser(IUser user, Map<String, I18nString> existingUsers);

    void removeUser(IUser user, Case groupCase);

    Map<String, I18nString> removeUser(HashSet<String> usersToRemove, Map<String, I18nString> existingUsers, Case groupCase);

    List<IUser> getMembers(Case groupCase);

    Set<String> getAllGroupsOfUser(IUser groupUser);

    String getGroupOwnerId(String groupId);

    Collection<String> getGroupsOwnerIds(Collection<String> groupIds);

    String getGroupOwnerEmail(String groupId);

    Collection<String> getGroupsOwnerEmails(Collection<String> groupIds);

    Set<String> getAllCoMembers(IUser user);

}
