//package com.netgrif.application.engine.auth.service.interfaces;
//
//import com.netgrif.core.auth.domain.IUser;
//import com.netgrif.core.auth.domain.IUser;
//import com.netgrif.core.auth.domain.LoggedUser;
//import com.netgrif.application.engine.petrinet.domain.PetriNet;
//import com.netgrif.core.petrinet.domain.roles.ProcessRole;
//import com.netgrif.application.engine.workflow.domain.ProcessResourceId;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.Authentication;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Set;
//
//public interface IUserService {
//
//    IUser findByAuth(Authentication auth, String realmId);
//
//    IUser saveUser(IUser user, String realmName);
//
//    IUser saveNewAndAuthenticate(IUser user, String realmId);
//
//    IUser saveNew(IUser user);
//
//    AnonymousUser saveNewAnonymous(AnonymousUser user);
//
//    IUser update(IUser user, IUser updates);
//
//    IUser findById(String id, String realmId);
//
//    IUser resolveById(String id, boolean small);
//
//    IUser findUserByUsername(String username, String realmName);
//
//    IUser findByEmail(String email, String realmId);
//
//    List<IUser> findAllUsers(String realmName);
//
//    Page<IUser> findAllCoMembers(LoggedUser loggedUser, Pageable pageable);
//
//    List<IUser> findAllByIds(Collection<String> ids, String realmId);
//
//    Page<IUser> findAllActiveByProcessRoles(Set<String> roleIds, Pageable pageable, String realmId);
//
//    void addDefaultRole(IUser user);
//    void addAnonymousRole(IUser user);
//
//    List<IUser> findAllByProcessRoles(Set<String> roleIds, String realmId);
//
//    void addDefaultAuthorities(IUser user);
//
//    IUser assignAuthority(String userId, String realmId, String authorityId);
//
//    IUser getLoggedOrSystem();
//
//    IUser getLoggedUser();
//
//    IUser getSystem();
//
//    LoggedUser getAnonymousLogged();
//
//    LoggedUser getLoggedUserFromContext();
//
//    IUser addRole(IUser user, String roleStringId);
//
//    Page<IUser> searchAllCoMembers(String query, LoggedUser principal, Pageable pageable);
//
//    IUser removeRole(IUser user, String roleStringId);
//
//    IUser removeRole(IUser user, ProcessRole processRole);
//
//    void removeRoleOfDeletedPetriNet(PetriNet net, String realmId);
//
//    void deleteUser(IUser user);
//
//    Page<IUser> searchAllCoMembers(String query, List<ProcessResourceId> roles, List<ProcessResourceId> negateRoleIds, LoggedUser principal, Pageable pageable);
//
//    IUser createSystemUser();
//
//}
