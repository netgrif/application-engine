package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface GroupService {

    Optional<Group> findByIdentifier(String identifier);

    Group create(AbstractUser user);

    Group create(String identifier, String title, AbstractUser user);

    Group getDefaultUserGroup(AbstractUser user);

    void addUserToDefaultSystemGroup(AbstractUser user);

    Group save(Group group);

    void delete(Group group);

    Group findById(String id);

    List<Group> findAllByIds(Collection<String> ids);

    Page<Group> findByIds(Collection<String> ids, Pageable pageable);

    Page<Group> findAll(Pageable pageable);

    Page<Group> findAllFromRealm(String realmId, Pageable pageable);

    void removeAllGroups();

    void removeAllByRealmId(String realmId);

    void removeAllByRealmIdIn(Collection<String> realmIds);

    Group getDefaultSystemGroup();

    Group addUser(String userId, String groupId, String realmId);

    Group addUser(String userId, Group group, String realmId);

    Group addUser(AbstractUser user, String groupIdentifier);

    Group addUser(AbstractUser user, Group group);

    Group removeUser(AbstractUser user, String groupIdentifier);

    Group removeUser(AbstractUser user, Group group);

    List<String> getAllCoMembers(AbstractUser user);

    Page<Group> findByPredicate(Predicate predicate, Pageable pageable);

    Group assignAuthority(String groupId, String authorityId);

    Pair<Group, Group> addSubgroup(String parentGroupId, String childGroupId);

    Pair<Group, Group> addSubgroup(Group parentGroup, String childGroupId);

    Pair<Group, Group> addSubgroup(String parentGroupId, Group childGroup);

    Pair<Group, Group> addSubgroup(Group parentGroup, Group childGroup);

    List<Group> getGroupParentGroupsById(String groupId);

    List<Group> getGroupParentGroups(Group group);

    List<Group> getGroupSubgroupsById(String groupId);

    List<Group> getGroupSubgroups(Group group);

    List<AbstractUser> getGroupMembersById(String groupId);

    List<AbstractUser> getGroupMembers(Group group);

    Collection<String> getGroupsOwnerEmails(Collection<String> groupIds);

    String getGroupOwnerEmail(String groupId);
}
