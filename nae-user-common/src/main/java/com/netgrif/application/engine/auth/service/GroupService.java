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

/**
 * Service interface for managing user groups in the application.
 * Provides methods for creating, retrieving, updating, and deleting groups,
 * as well as managing group memberships and hierarchies.
 */
public interface GroupService {

    /**
     * Finds a group by its unique identifier.
     *
     * @param identifier the unique identifier of the group
     * @return an {@link Optional} containing the group if found, empty otherwise
     */
    Optional<Group> findByIdentifier(String identifier);

    /**
     * Creates a new group for a given user.
     *
     * @param groupOwner the user for whom to create the group
     * @return the newly created {@link Group}
     */
    Group create(AbstractUser groupOwner);

    /**
     * Creates a new group with specified parameters.
     *
     * @param identifier the unique identifier for the group
     * @param title the display title of the group
     * @param groupOwner the user associated with the group creation
     * @return the newly created {@link Group}
     */
    Group create(String identifier, String title, AbstractUser groupOwner);

    /**
     * Retrieves the default user group for a given user.
     *
     * @param user the user whose default group is to be retrieved
     * @return the default {@link Group} for the user
     */
    Group getDefaultUserGroup(AbstractUser user);

    /**
     * Adds a user to the default system group.
     *
     * @param user the user to be added to the default system group
     */
    void addUserToDefaultSystemGroup(AbstractUser user);

    /**
     * Persists a group to the database.
     *
     * @param group the group to be saved
     * @return the saved {@link Group}
     */
    Group save(Group group);

    /**
     * Deletes a group from the system.
     *
     * @param group the group to be deleted
     */
    void delete(Group group);

    /**
     * Finds a group by its ID.
     *
     * @param id the ID of the group
     * @return the {@link Group} with the specified ID
     */
    Group findById(String id);

    Page<Group> findAllByIds(Collection<String> ids, Pageable pageable);

    /**
     * Retrieves all groups with pagination support.
     *
     * @param pageable pagination information
     * @return page of all {@link Group}s
     */
    Page<Group> findAll(Pageable pageable);

    /**
     * Finds all groups from a specific realm with pagination.
     *
     * @param realmId ID of the realm
     * @param pageable pagination information
     * @return page of {@link Group}s in the specified realm
     */
    Page<Group> findAllFromRealm(String realmId, Pageable pageable);

    /**
     * Finds all groups from specified realms with pagination.
     *
     * @param realmIds collection of realm IDs
     * @param pageable pagination information
     * @return page of {@link Group}s in the specified realms
     */
    Page<Group> findAllFromRealmIn(Collection<String> realmIds, Pageable pageable);

    /**
     * Removes all groups from the system.
     */
    void removeAllGroups();

    /**
     * Removes all groups from a specific realm.
     *
     * @param realmId ID of the realm whose groups should be removed
     */
    void removeAllByRealmId(String realmId);

    /**
     * Removes all groups from specified realms.
     *
     * @param realmIds collection of realm IDs whose groups should be removed
     */
    void removeAllByRealmIdIn(Collection<String> realmIds);

    /**
     * Retrieves the default system group.
     *
     * @return the default system {@link Group}
     */
    Group getDefaultSystemGroup();

    /**
     * Adds a user to a group within a specific realm.
     *
     * @param userId ID of the user to add
     * @param groupId ID of the group
     * @param realmId ID of the realm
     * @return the updated {@link Group}
     */
    Group addUser(String userId, String groupId, String realmId);

    /**
     * Adds a user to a group within a specific realm.
     *
     * @param userId ID of the user to add
     * @param group the group to add the user to
     * @param realmId ID of the realm
     * @return the updated {@link Group}
     */
    Group addUser(String userId, Group group, String realmId);

    /**
     * Adds a user to a group specified by identifier.
     *
     * @param user the user to add
     * @param groupIdentifier identifier of the target group
     * @return the updated {@link Group}
     */
    Group addUser(AbstractUser user, String groupIdentifier);

    /**
     * Adds a user to a specific group.
     *
     * @param user the user to add
     * @param group the group to add the user to
     * @return the updated {@link Group}
     */
    Group addUser(AbstractUser user, Group group);

    /**
     * Removes a user from a group specified by identifier.
     *
     * @param user the user to remove
     * @param groupIdentifier identifier of the target group
     * @return the updated {@link Group}
     */
    Group removeUser(AbstractUser user, String groupIdentifier);

    /**
     * Removes a user from a specific group.
     *
     * @param user the user to remove
     * @param group the group to remove the user from
     * @return the updated {@link Group}
     */
    Group removeUser(AbstractUser user, Group group);

    /**
     * Finds groups matching a given predicate with pagination.
     *
     * @param predicate the search predicate
     * @param pageable pagination information
     * @return page of {@link Group}s matching the predicate
     */
    Page<Group> findByPredicate(Predicate predicate, Pageable pageable);

    /**
     * Assigns an authority to a group.
     *
     * @param groupId ID of the target group
     * @param authorityId ID of the authority to assign
     * @return the updated {@link Group}
     */
    Group assignAuthority(String groupId, String authorityId);

    /**
     * Adds a subgroup relationship between two groups.
     *
     * @param parentGroupId ID of the parent group
     * @param childGroupId ID of the child group
     * @return a {@link Pair} containing both the updated parent and child {@link Group}s
     */
    Pair<Group, Group> addSubgroup(String parentGroupId, String childGroupId);

    /**
     * Adds a subgroup relationship between two groups.
     *
     * @param parentGroup the parent group
     * @param childGroupId ID of the child group
     * @return a {@link Pair} containing both the updated parent and child {@link Group}s
     */
    Pair<Group, Group> addSubgroup(Group parentGroup, String childGroupId);

    /**
     * Adds a subgroup relationship between two groups.
     *
     * @param parentGroupId ID of the parent group
     * @param childGroup the child group
     * @return a {@link Pair} containing both the updated parent and child {@link Group}s
     */
    Pair<Group, Group> addSubgroup(String parentGroupId, Group childGroup);

    /**
     * Adds a subgroup relationship between two groups.
     *
     * @param parentGroup the parent group
     * @param childGroup the child group
     * @return a {@link Pair} containing both the updated parent and child {@link Group}s
     */
    Pair<Group, Group> addSubgroup(Group parentGroup, Group childGroup);

    /**
     * Retrieves all parent groups of a specified group.
     *
     * @param groupId ID of the group
     * @return list of parent {@link Group}s
     */
    List<Group> getGroupParentGroupsById(String groupId);

    /**
     * Retrieves all parent groups of a specified group.
     *
     * @param group the group whose parent groups are to be retrieved
     * @return list of parent {@link Group}s
     */
    List<Group> getGroupParentGroups(Group group);

    /**
     * Retrieves all subgroups of a specified group.
     *
     * @param groupId ID of the group
     * @return list of child {@link Group}s
     */
    List<Group> getGroupSubgroupsById(String groupId);

    /**
     * Retrieves all subgroups of a specified group.
     *
     * @param group the group whose subgroups are to be retrieved
     * @return list of child {@link Group}s
     */
    List<Group> getGroupSubgroups(Group group);

    /**
     * Retrieves email addresses of the owners of specified groups.
     *
     * @param groupIds collection of group IDs
     * @return collection of owner email addresses
     */
    List<String> getGroupsOwnerEmails(Collection<String> groupIds);

    /**
     * Retrieves the email address of a specified group's owner.
     *
     * @param groupId ID of the group
     * @return email address of the group owner
     */
    String getGroupOwnerEmail(String groupId);
}
