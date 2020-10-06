package com.netgrif.workflow.orgstructure.groups.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Case;
import com.querydsl.core.types.Predicate;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface INextGroupService {

    Case createGroup(User author);

    Case createGroup(String title, User author);

    Case findGroup(String groupID);

    List<Case> findAllGroups();

    Case findDefaultGroup();

    List<Case> findByPredicate(Predicate predicate);

    Map<String, I18nString> inviteUser(String email, Map<String, I18nString> existingUsers, Case groupCase);

    void addUser(User user, Case groupCase);

    Map<String, I18nString> addUser(User user, Map<String, I18nString> existingUsers);

    void removeUser(User user, Case groupCase);

    Map<String, I18nString> removeUser(HashSet<String> usersToRemove, Map<String, I18nString> existingUsers, Case groupCase);

    List<User> getMembers(Case groupCase);

    Long getGroupOwnerId(String groupId);

    Collection<Long> getGroupsOwnerIds(Collection<String> groupIds);
}
