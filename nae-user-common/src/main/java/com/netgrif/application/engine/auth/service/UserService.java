package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserService {

    AbstractUser saveUser(AbstractUser user, String realmId);

    AbstractUser saveUser(AbstractUser user);

    List<User> saveUsers(Collection<AbstractUser> users);

    void deleteAllUsers(Collection<String> realmIds);

    void deleteAllUsers();

    Optional<AbstractUser> findUserByUsername(String username, String realmName);

    Page<AbstractUser> findAllUsers(String realmName, Pageable pageable);

    AbstractUser createUser(String username, String email, String firstName, String lastName, String password, String realmName);

    AbstractUser createUser(AbstractUser user, String realmId);

    AbstractUser createUserFromThirdParty(String username, String email, String firstName, String lastName, String realmId, String authMethod);

    void addDefaultRole(AbstractUser user);

    void addAnonymousAuthorities(AbstractUser user);

    void addAllRolesToAdminByUsername(String username);

    void addAnonymousRole(AbstractUser user);

    AbstractUser findById(String id, String realmId);

    void deleteUser(AbstractUser user);

    AbstractUser findByAuth(Authentication auth, String realmId);

    AbstractUser update(AbstractUser user, AbstractUser updatedUser);

    AbstractUser findByEmail(String email, String realmId);

    Page<AbstractUser> findAllCoMembers(LoggedUser loggedUser, Pageable pageable);

    List<AbstractUser> findAllByIds(Collection<String> ids, String realmId);

    Page<AbstractUser> findAllActiveByProcessRoles(Collection<ProcessResourceId> roleIds, Pageable pageable);

    Page<AbstractUser> findAllActiveByProcessRoles(Collection<ProcessResourceId> roleIds, Pageable pageable, Collection<String> realmIds);

    List<AbstractUser> findAllByProcessRoles(Collection<ProcessResourceId> roleIds, Collection<String> realmIds);

    List<AbstractUser> findAllByProcessRoles(Collection<ProcessResourceId> roleIds);

    void addDefaultAuthorities(AbstractUser user);

    AbstractUser assignAuthority(String userId, String realmId, String authorityId);

    AbstractUser getLoggedOrSystem();

    AbstractUser getLoggedUser();

    AbstractUser getSystem();

    LoggedUser getLoggedUserFromContext();

    AbstractUser addRole(AbstractUser user, ProcessResourceId id);

    AbstractUser addRole(AbstractUser user, String roleStringId);

    Page<AbstractUser> searchAllCoMembers(String query, LoggedUser principal, Pageable pageable);

    Page<AbstractUser> searchAllCoMembers(String query, Collection<ProcessResourceId> roleIds, Collection<ProcessResourceId> negateRoleIds, LoggedUser loggedUser, Pageable pageable);

    AbstractUser removeRolesById(AbstractUser user, Collection<ProcessResourceId> processRolesIds);

    AbstractUser removeRoles(AbstractUser user, Collection<ProcessRole> processRoles);

    AbstractUser removeRole(AbstractUser user, ProcessRole role);

    AbstractUser removeRole(AbstractUser user, ProcessResourceId roleId);

    AbstractUser removeRole(AbstractUser user, String roleId);

    void removeRoleOfDeletedPetriNet(PetriNet process, Collection<String> realmIds);

    AbstractUser createSystemUser();

    AbstractUser transformToUser(ActorRef author);

    AbstractUser transformToUser(LoggedUser loggedUser);

    void removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, Collection<String> realmIds);

    List<User> findAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, Collection<String> realmIds);

    List<Group> getUserGroups(AbstractActor actor);
}
