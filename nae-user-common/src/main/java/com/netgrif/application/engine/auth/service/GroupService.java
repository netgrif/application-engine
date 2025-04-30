package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface GroupService {

    Optional<Group> findByIdentifier(String identifier);

    Group create(IUser user);

    Group create(String identifier, String title, IUser user);

    Group getDefaultUserGroup(IUser user);

    void addUserToDefaultSystemGroup(IUser user);

    Group save(Group group);

    void delete(Group group);

    Group findById(String id);

    Set<Group> findAllByIds(Set<String> ids);

    Set<Group> findAll();

    Group getDefaultSystemGroup();

    Group addUser(String userId, String groupId, String realmId);

    Group addUser(IUser user, String groupIdentifier);

    Group addUser(IUser user, Group group);

    Group removeUser(IUser user, String groupIdentifier);

    Group removeUser(IUser user, Group group);

    void populateMembers(Group group);

    Set<String> getAllCoMembers(IUser user);

    Page<Group> findByPredicate(Predicate predicate, Pageable pageable);
}
