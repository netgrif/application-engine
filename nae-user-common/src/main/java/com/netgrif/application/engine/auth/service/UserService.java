package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.objects.auth.domain.Author;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.auth.domain.User;
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
import java.util.Set;

public interface UserService {

    IUser saveUser(IUser user, String realmId);

    IUser saveUser(IUser user);

    List<User> saveUsers(List<IUser> users);

    void deleteAllUsers(Collection<String> realmIds);

    Optional<IUser> findUserByUsername(String username, String realmName);

    Page<IUser> findAllUsers(String realmName, Pageable pageable);

    IUser createUser(String username, String email, String firstName, String lastName, String password, String realmName);

    IUser createUser(IUser user, String realmId);

    IUser createUserFromThirdParty(String username, String email, String firstName, String lastName, String realmId, String authMethod);

    void addDefaultRole(IUser user);

    void addAnonymousAuthorities(IUser user);

    void addAllRolesToAdminByUsername(String username);

    void addAnonymousRole(IUser user);

    IUser findById(String id, String realmId);

    void deleteUser(IUser user);

    IUser findByAuth(Authentication auth, String realmId);

    IUser update(IUser user, IUser updatedUser);

    IUser findByEmail(String email, String realmId);

    Page<IUser> findAllCoMembers(com.netgrif.application.engine.objects.auth.domain.LoggedUser loggedUser, Pageable pageable);

    List<IUser> findAllByIds(Collection<String> ids, String realmId);

    Page<IUser> findAllActiveByProcessRoles(Set<ProcessResourceId> roleIds, Pageable pageable);

    Page<IUser> findAllActiveByProcessRoles(Set<ProcessResourceId> roleIds, Pageable pageable, Collection<String> realmIds);

    List<IUser> findAllByProcessRoles(Set<ProcessResourceId> roleIds, Collection<String> realmIds);

    List<IUser> findAllByProcessRoles(Set<ProcessResourceId> roleIds);

    void addDefaultAuthorities(IUser user);

    IUser assignAuthority(String userId, String realmId, String authorityId);

    IUser getLoggedOrSystem();

    IUser getLoggedUser();

    IUser getSystem();

    com.netgrif.application.engine.objects.auth.domain.LoggedUser getLoggedUserFromContext();

    IUser addRole(IUser user, ProcessResourceId id);

    IUser addRole(IUser user, String roleStringId);

    IUser addNegativeProcessRole(IUser user, ProcessResourceId id);

    IUser addNegativeProcessRole(IUser user, String roleStringId);

    Page<IUser> searchAllCoMembers(String query, com.netgrif.application.engine.objects.auth.domain.LoggedUser principal, Pageable pageable);

    Page<IUser> searchAllCoMembers(String query, List<ProcessResourceId> roleIds, List<ProcessResourceId> negateRoleIds, com.netgrif.application.engine.objects.auth.domain.LoggedUser loggedUser, Pageable pageable);

    IUser removeRole(IUser user, ProcessRole role);

    IUser removeRole(IUser user, ProcessResourceId roleId);

    IUser removeRole(IUser user, String roleId);

    IUser removeNegativeProcessRole(IUser user, ProcessRole role);

    IUser removeNegativeProcessRole(IUser user, ProcessResourceId roleId);

    IUser removeNegativeProcessRole(IUser user, String roleId);

    void removeRoleOfDeletedPetriNet(PetriNet process, Collection<String> realmIds);

    IUser createSystemUser();

    IUser transformToUser(LoggedUserImpl loggedUser);

    LoggedUserImpl transformToLoggedUser(IUser user);

   void removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, Collection<String> realmIds);

    List<User> findAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, Collection<String> realmIds);

    IUser transformToUser(Author author);

    Author transformToAuthor(IUser user);

    void populateGroups(IUser user);
}
