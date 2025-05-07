package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.Optional;
import java.util.Set;

public interface GroupService {

    Optional<Group> findByIdentifier(String identifier);

    Group create(AbstractUser user);

    Group create(String identifier, String title, AbstractUser user);

    Group getDefaultUserGroup(AbstractUser user);

    void addUserToDefaultSystemGroup(AbstractUser user);

    Group save(Group group);

    void delete(Group group);

    Group findById(String id);

    List<Group> findByIds(Collection<String> ids);

    Page<Group> findAllByIds(Set<String> ids, Pageable pageable);

    Page<Group> findAll(Pageable pageable);

    Page<Group> findAllFromRealm(String realmId, Pageable pageable);

    void removeAllByRealmId(String realmId);

    void removeAllByRealmIdInSet(Set<String> realmIds);

    Group getDefaultSystemGroup();

    Group addUser(String userId, String groupId, String realmId);

    Group addUser(String userId, Group group, String realmId);

    Group addUser(AbstractUser user, String groupIdentifier);

    Group addUser(AbstractUser user, Group group);

    Group removeUser(AbstractUser user, String groupIdentifier);

    Group removeUser(AbstractUser user, Group group);

    Set<String> getAllCoMembers(AbstractUser user);

    Page<Group> findByPredicate(Predicate predicate, Pageable pageable);

    Group assignAuthority(String groupId, String authorityId);

    Pair<Group, Group> addSubgroup(String parentGroupId, String childGroupId);

    Pair<Group, Group> addSubgroup(Group parentGroup, String childGroupId);

    Pair<Group, Group> addSubgroup(String parentGroupId, Group childGroup);

    Pair<Group, Group> addSubgroup(Group parentGroup, Group childGroup);

    Set<Group> getGroupParentGroupsById(String groupId);

    Set<Group> getGroupParentGroups(Group group);

    Set<Group> getGroupSubgroupsById(String groupId);

    Set<Group> getGroupSubgroups(Group group);

    Set<AbstractUser> getGroupMembersById(String groupId);

    Set<AbstractUser> getGroupMembers(Group group);

    Collection<String> getGroupsOwnerEmails(Collection<String> groupIds);

    String getGroupOwnerEmail(String groupId);
}
