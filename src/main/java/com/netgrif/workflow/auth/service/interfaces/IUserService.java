package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.AnonymousUser;
import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.workflow.orgstructure.domain.Member;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface IUserService {

    IUser findByAuth(Authentication auth);

    IUser save(IUser user);

    IUser saveNew(IUser user);

    AnonymousUser saveNewAnonymous(AnonymousUser user);

    IUser update(IUser user, UpdateUserRequest updates);

    Member upsertGroupMember(IUser user);

    IUser findById(String id, boolean small);

    IUser resolveById(String id, boolean small);

    IUser findByEmail(String email, boolean small);

    IUser findAnonymousByEmail(String email, boolean small);

    List<IUser> findAll(boolean small);

    Page<IUser> findAllCoMembers(LoggedUser loggedUser, boolean small, Pageable pageable);

    Page<IUser> findAllActiveByProcessRoles(Set<String> roleIds, boolean small, Pageable pageable);

    List<IUser> findAllByProcessRoles(Set<String> roleIds, boolean small);

    void addDefaultRole(IUser user);

    void addDefaultAuthorities(IUser user);

    void assignAuthority(String userId, String authorityId);

    IUser getLoggedOrSystem();

    IUser getLoggedUser();

    IUser getSystem();

    LoggedUser getAnonymousLogged();

    IUser addRole(IUser user, String roleStringId);

    Page<IUser> searchAllCoMembers(String query, LoggedUser principal, Boolean small, Pageable pageable);

    IUser removeRole(IUser user, String roleStringId);

    void deleteUser(IUser user);

    Page<IUser> searchAllCoMembers(String query, List<ObjectId> roles, List<ObjectId> negateRoleIds, LoggedUser principal, Boolean small, Pageable pageable);

    IUser createSystemUser();
}