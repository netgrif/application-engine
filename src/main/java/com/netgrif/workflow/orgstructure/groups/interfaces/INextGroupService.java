package com.netgrif.workflow.orgstructure.groups.interfaces;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Case;
import com.querydsl.core.types.Predicate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.*;

@ConditionalOnProperty(value = "nae.group.default.enabled",
        havingValue = "true",
        matchIfMissing = true)
public interface INextGroupService {

    Case createDefaultSystemGroup(IUser author);

    Case createGroup(IUser author);

    Case createGroup(String title, IUser author);

    Case findGroup(String groupID);

    List<Case> findByIds(Collection<String> groupIds);

    List<Case> findAllGroups();

    Case findDefaultGroup();

    List<Case> findByPredicate(Predicate predicate);

    Map<String, I18nString> inviteUser(String email, Map<String, I18nString> existingUsers, Case groupCase);

    void addUserToDefaultGroup(IUser user);

    void addUser(IUser user, Case groupCase);

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

    Set<String> getAllCoMembers(User user);

}
