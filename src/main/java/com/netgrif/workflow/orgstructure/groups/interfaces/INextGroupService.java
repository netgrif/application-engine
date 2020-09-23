package com.netgrif.workflow.orgstructure.groups.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Case;
import com.querydsl.core.types.Predicate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface INextGroupService {

    Case findGroup(String groupID);

    List<Case> findAllGroups();

    List<Case> findByPredicate(Predicate predicate);

    Map<String, I18nString> addUser(User user, Map<String, I18nString> existingUsers);

    Map<String, I18nString> removeUser(HashSet<String> usersToRemove, Map<String, I18nString> existingUsers);

    List<User> getMembers(Case groupCase);

    Map<String, I18nString> listToMap(List<Case> cases);
}
