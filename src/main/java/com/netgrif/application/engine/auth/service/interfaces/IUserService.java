package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.AnonymousUser;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
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

    IUser findById(String id, boolean small);

    IUser resolveById(String id, boolean small);

    IUser findByEmail(String email, boolean small);

    IUser findAnonymousByEmail(String email, boolean small);

    List<IUser> findAll(boolean small);

    Page<IUser> findAllCoMembers(LoggedUser loggedUser, boolean small, Pageable pageable);

    List<IUser> findAllByIds(Set<String> ids, boolean small);

    Page<IUser> findAllActiveByProcessRoles(Set<String> roleIds, boolean small, Pageable pageable);

    void addDefaultRole(IUser user);

    List<IUser> findAllByProcessRoles(Set<String> roleIds, boolean small);

    void addDefaultAuthorities(IUser user);

    IUser assignAuthority(String userId, String authorityId);

    IUser getLoggedOrSystem();

    IUser getLoggedUser();

    IUser getSystem();

    LoggedUser getAnonymousLogged();

    LoggedUser getLoggedUserFromContext();

    IUser addRole(IUser user, String roleStringId);

    Page<IUser> searchAllCoMembers(String query, LoggedUser principal, Boolean small, Pageable pageable);

    IUser removeRole(IUser user, String roleStringId);

    void removeRoleOfDeletedPetriNet(PetriNet net);

    void deleteUser(IUser user);

    Page<IUser> searchAllCoMembers(String query, List<ObjectId> roles, List<ObjectId> negateRoleIds, LoggedUser principal, Boolean small, Pageable pageable);

    IUser createSystemUser();

}
