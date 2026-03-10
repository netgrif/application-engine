package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for managing user-related operations in the application.
 * Provides functionality for user CRUD operations, role management, and user authentication.
 * Supports multi-realm user management and handles both system and regular users.
 */
public interface UserService {

    /**
     * Saves a user in the specified realm.
     *
     * @param user the user to be saved
     * @param realmId the identifier of the realm
     * @return the saved user
     */
    AbstractUser saveUser(AbstractUser user, String realmId);

    /**
     * Saves a user without specifying a realm.
     *
     * @param user the user to be saved
     * @return the saved user
     */
    AbstractUser saveUser(AbstractUser user);

    /**
     * Saves multiple users in batch.
     *
     * @param users collection of users to be saved
     * @return list of saved users
     */
    List<User> saveUsers(Collection<AbstractUser> users);

    /**
     * Deletes all users from specified realms.
     *
     * @param realmIds collection of realm identifiers
     */
    void deleteAllUsers(Collection<String> realmIds);

    /**
     * Deletes all users from the system.
     */
    void deleteAllUsers();

    /**
     * Finds a user by username within a specific realm.
     *
     * @param username the username to search for
     * @param realmName the name of the realm
     * @return an Optional containing the user if found
     */
    Optional<AbstractUser> findUserByUsername(String username, String realmName);

    Page<AbstractUser> findAllUsersByQuery(Query query, String realmName, Pageable pageable);

    /**
     * Retrieves a paginated list of all users in a specific realm.
     *
     * @param realmName the name of the realm
     * @param pageable pagination information
     * @return page of users
     */
    Page<AbstractUser> findAllUsers(String realmName, Pageable pageable);

    /**
     * Creates a new user with basic information.
     *
     * @param username the username
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     * @param password the password
     * @param realmName the realm name
     * @return the created user
     */
    AbstractUser createUser(String username, String email, String firstName, String lastName, String password, String realmName);

    /**
     * Creates a new user from an existing user object in a specific realm.
     *
     * @param user the user to create
     * @param realmId the realm identifier
     * @return the created user
     */
    AbstractUser createUser(AbstractUser user, String realmId);

    /**
     * Creates a user from third-party authentication.
     *
     * @param username the username
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     * @param realmId the realm identifier
     * @param authMethod the authentication method used
     * @return the created user
     */
    AbstractUser createUserFromThirdParty(String username, String email, String firstName, String lastName, String realmId, String authMethod);

    /**
     * Adds default role to a user.
     *
     * @param user the user to update
     */
    void addDefaultRole(AbstractUser user);

    /**
     * Adds default attributes to a user.
     *
     * @param user the user to update
     */
    void addDefaultAttributes(AbstractUser user);

    /**
     * Adds anonymous authorities to a user.
     *
     * @param user the user to update
     */
    void addAnonymousAuthorities(AbstractUser user);

    /**
     * Adds all available roles to an admin user.
     *
     * @param username the username of the admin
     */
    void addAllRolesToAdminByUsername(String username);

    /**
     * Adds anonymous role to a user.
     *
     * @param user the user to update
     */
    void addAnonymousRole(AbstractUser user);

    /**
     * Finds a user by ID in a specific realm.
     *
     * @param id the user identifier
     * @param realmId the realm identifier
     * @return the found user
     */
    AbstractUser findById(String id, String realmId);

    /**
     * Deletes a user from the system.
     *
     * @param user the user to delete
     */
    void deleteUser(AbstractUser user);

    /**
     * Finds a user by authentication in a specific realm.
     *
     * @param auth the authentication object
     * @param realmId the realm identifier
     * @return the found user
     */
    AbstractUser findByAuth(Authentication auth, String realmId);

    /**
     * Updates a user with new information.
     *
     * @param user the current user
     * @param updatedUser the user with updated information
     * @return the updated user
     */
    AbstractUser update(AbstractUser user, AbstractUser updatedUser);

    /**
     * Finds a user by email in a specific realm.
     *
     * @param email the email address
     * @param realmId the realm identifier
     * @return the found user
     */
    AbstractUser findByEmail(String email, String realmId);

    /**
     * Finds all users by their IDs in a specific realm.
     *
     * @param ids collection of user identifiers
     * @param realmId the realm identifier
     * @return list of found users
     */
    Page<AbstractUser> findAllByIds(Collection<String> ids, String realmId, Pageable pageable);

    /**
     * Finds all active users with specific process roles.
     *
     * @param roleIds collection of process role identifiers
     * @param pageable pagination information
     * @return page of users
     */
    Page<AbstractUser> findAllActiveByProcessRoles(Collection<ProcessResourceId> roleIds, Pageable pageable, String realmId);

    /**
     * Finds all users with specific process roles in specific realms.
     *
     * @param roleIds collection of process role identifiers
     * @param realmId realm identifier
     * @return list of users
     */
    Page<AbstractUser> findAllByProcessRoles(Collection<ProcessResourceId> roleIds, String realmId, Pageable pageable);

    /**
     * Adds default authorities to a user.
     *
     * @param user the user to update
     */
    void addDefaultAuthorities(AbstractUser user);

    /**
     * Assigns an authority to a user.
     *
     * @param userId the user identifier
     * @param realmId the realm identifier
     * @param authorityId the authority identifier
     * @return the updated user
     */
    AbstractUser assignAuthority(String userId, String realmId, String authorityId);

    /**
     * Gets the currently logged user or system user if no user is logged in.
     *
     * @return the logged user or system user
     */
    AbstractUser getLoggedOrSystem();

    /**
     * Gets the currently logged user.
     *
     * @return the logged user
     */
    AbstractUser getLoggedUser();

    /**
     * Gets the system user.
     *
     * @return the system user
     */
    AbstractUser getSystem();

    /**
     * Gets the logged user from the current security context.
     *
     * @return the logged user
     */
    LoggedUser getLoggedUserFromContext();

    /**
     * Adds a process role to a user by ID.
     *
     * @param user the user to update
     * @param id the process role identifier
     * @return the updated user
     */
    AbstractUser addRole(AbstractUser user, ProcessResourceId id);

    /**
     * Adds a role to a user by string identifier.
     *
     * @param user the user to update
     * @param roleStringId the role string identifier
     * @return the updated user
     */
    AbstractUser addRole(AbstractUser user, String roleStringId);

    Page<AbstractUser> findAllCoMembers(LoggedUser loggedUser, Pageable pageable);

    /**
     * Searches for co-members of a principal user.
     *
     * @param query the search query
     * @param principal the principal user
     * @param pageable pagination information
     * @return page of matching co-members
     */
    Page<AbstractUser> searchAllCoMembers(String query, LoggedUser principal, Pageable pageable);

    /**
     * Advanced search for co-members with role filtering.
     *
     * @param query the search query
     * @param roleIds required role identifiers
     * @param negateRoleIds excluded role identifiers
     * @param loggedUser the logged user
     * @param pageable pagination information
     * @return page of matching co-members
     */
    Page<AbstractUser> searchAllCoMembers(String query, Collection<ProcessResourceId> roleIds,
            Collection<ProcessResourceId> negateRoleIds, LoggedUser loggedUser, Pageable pageable);

    /**
     * Removes specified process roles from a user.
     *
     * @param user the user to update
     * @param processRolesIds collection of process role identifiers to remove
     * @return the updated user
     */
    AbstractUser removeRolesById(AbstractUser user, Collection<ProcessResourceId> processRolesIds);

    /**
     * Removes specified process roles from a user.
     *
     * @param user the user to update
     * @param processRoles collection of process roles to remove
     * @return the updated user
     */
    AbstractUser removeRoles(AbstractUser user, Collection<ProcessRole> processRoles);

    /**
     * Removes a specific process role from a user.
     *
     * @param user the user to update
     * @param role the process role to remove
     * @return the updated user
     */
    AbstractUser removeRole(AbstractUser user, ProcessRole role);

    /**
     * Removes a process role from a user by role ID.
     *
     * @param user the user to update
     * @param roleId the process role identifier
     * @return the updated user
     */
    AbstractUser removeRole(AbstractUser user, ProcessResourceId roleId);

    /**
     * Removes a role from a user by string identifier.
     *
     * @param user the user to update
     * @param roleId the role string identifier
     * @return the updated user
     */
    AbstractUser removeRole(AbstractUser user, String roleId);

    /**
     * Removes roles associated with a deleted Petri net from users in specified realms.
     *
     * @param process the deleted Petri net
     */
    void removeRoleOfDeletedPetriNet(PetriNet process);

    /**
     * Removes roles associated with a deleted Petri net from users in specified realms.
     *
     * @param petriNetRoles roles of deleted Petri net
     */
    void removeRoleOfDeletedPetriNet(Set<ProcessRole> petriNetRoles);

    /**
     * Creates a system user.
     *
     * @return the created system user
     */
    AbstractUser createSystemUser();

    /**
     * Transforms an actor reference to a user.
     *
     * @param author the actor reference to transform
     * @return the transformed user
     */
    AbstractUser transformToUser(ActorRef author);

    /**
     * Transforms a logged user to a regular user.
     *
     * @param loggedUser the logged user to transform
     * @return the transformed user
     */
    AbstractUser transformToUser(LoggedUser loggedUser);

    /**
     * Removes all users with specified state and expiration date before given date in specified realms.
     *
     * @param state the user state
     * @param expirationDate the expiration date
     * @param realmIds collection of realm identifiers
     */
    void removeAllByStateAndExpirationDateBeforeForRealms(UserState state, LocalDateTime expirationDate, Collection<String> realmIds);

    Page<User> findAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, String realmIds, Pageable pageable);

    void removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, String realmId);

    /**
     * Gets all groups associated with an actor.
     *
     * @param actor the actor for which to retrieve groups
     * @return list of groups
     */
    List<Group> getUserGroups(AbstractActor actor);


    /**
     * Assigns the provided process roles to all admin users in the system, updating their current set of roles.
     * Behavior: roles are added (union) and duplicates are ignored; no existing roles are removed.
     *
     * @param roles collection of process roles to assign to admin users
     */
    void updateAdminWithRoles(Collection<ProcessRole> roles);

    /**
     * Resets password for user.
     *
     * @param user user
     * @param newPassword new password
     * @param oldPassword old password
     * @return the updated user
     */
    AbstractUser changePassword(AbstractUser user, String newPassword, String oldPassword);

    /**
     * Searches for users in the specified realm based on the provided predicate and pagination parameters.
     *
     * @param predicate the query conditions to filter users
     * @param pageable  the pagination parameters for the search results
     * @param realmId   the name of the realm, used to determine which collection to query
     * @return a paginated list of users matching the predicate within the specified realm
     */
    Page<User> search(Predicate predicate, Pageable pageable, String realmId);

    // todo javadoc
    Page<AbstractUser> findAllByWorkspace(String workspaceId, String realmId, Pageable pageable);
}
