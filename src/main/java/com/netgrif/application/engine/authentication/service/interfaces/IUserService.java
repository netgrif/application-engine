package com.netgrif.application.engine.authentication.service.interfaces;

import com.netgrif.application.engine.authentication.domain.AnonymousUser;
import com.netgrif.application.engine.authentication.domain.IUser;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.web.requestbodies.UpdateUserRequest;
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

    Page<IUser> findAllCoMembers(Identity identity, Pageable pageable);

    List<IUser> findAllByIds(Set<String> ids);

    Page<IUser> findAllActiveByRoles(Set<String> roleIds, Pageable pageable);

    void addDefaultRole(IUser user);

    void addDefaultAuthorities(IUser user);

    IUser assignAuthority(String userId, String authorityId);

    IUser getLoggedOrSystem();

    IUser getLoggedUser();

    IUser getSystem();

    Identity getAnonymousLogged();

    Identity getLoggedUserFromContext();

    Page<IUser> searchAllCoMembers(String query, Identity principal, Pageable pageable);

    void deleteUser(IUser user);

    Page<IUser> searchAllCoMembers(String query, List<ObjectId> roles, List<ObjectId> negateRoleIds, Identity principal, Pageable pageable);

    IUser createSystemUser();

    boolean existsById(String id);

    IUser getUserFromLoggedUser(Identity identity);
}
