package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.AnonymousUser;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.application.engine.petrinet.domain.Process;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;

public interface IUserService {

    IUser findByAuth(Authentication auth);

    IUser save(IUser user);

    IUser saveNewAndAuthenticate(IUser user);

    IUser saveNew(IUser user);

    AnonymousUser saveNewAnonymous(AnonymousUser user);

    IUser update(IUser user, UpdateUserRequest updates);

    IUser findById(String id);

    IUser resolveById(String id);

    IUser findByEmail(String email);

    IUser findAnonymousByEmail(String email);

    List<IUser> findAll();

    Page<IUser> findAllCoMembers(LoggedUser loggedUser, Pageable pageable);

    List<IUser> findAllByIds(Set<String> ids);

    Page<IUser> findAllActiveByProcessRoles(Set<String> roleIds, Pageable pageable);

    void addDefaultRole(IUser user);

    List<IUser> findAllByProcessRoles(Set<String> roleIds);

    void addDefaultAuthorities(IUser user);

    IUser assignAuthority(String userId, String authorityId);

    IUser getLoggedOrSystem();

    IUser getLoggedUser();

    IUser getSystem();

    LoggedUser getAnonymousLogged();

    LoggedUser getLoggedUserFromContext();

    IUser addRole(IUser user, String roleStringId);

    Page<IUser> searchAllCoMembers(String query, LoggedUser principal, Pageable pageable);

    IUser removeRole(IUser user, String roleStringId);

    void removeRoleOfDeletedPetriNet(Process net);

    void deleteUser(IUser user);

    Page<IUser> searchAllCoMembers(String query, List<ObjectId> roles, List<ObjectId> negateRoleIds, LoggedUser principal, Pageable pageable);

    IUser createSystemUser();

    boolean existsById(String id);

    IUser getUserFromLoggedUser(LoggedUser loggedUser);
}
