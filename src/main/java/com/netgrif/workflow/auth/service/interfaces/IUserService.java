package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.workflow.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.workflow.orgstructure.domain.Member;
import com.netgrif.workflow.workflow.service.interfaces.ITaskAuthenticationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface IUserService {

    User findByAuth(Authentication auth);

    User save(User user);

    User saveNew(User user);

    User update(User user, UpdateUserRequest updates);

    Member upsertGroupMember(User user);

    User findById(Long id, boolean small);

    User findByEmail(String email, boolean small);

    List<User> findAll(boolean small);

    Page<User> findAllCoMembers(LoggedUser loggedUser, boolean small, Pageable pageable);

    Page<User> findAllActiveByProcessRoles(Set<String> roleIds, boolean small, Pageable pageable);

    void assignAuthority(Long userId, Long authorityId);

    void addDefaultRole(User user);

    void addDefaultAuthorities(User user);

    void encodeUserPassword(User user);

    boolean stringMatchesUserPassword(User user, String passwordToCompare);

    User getLoggedOrSystem();

    User getLoggedUser();

    void checkUsersPermissions(String taskId, ITaskAuthenticationService taskAuthenticationService) throws UnauthorisedRequestException;

    User getSystem();

    User addRole(User user, String roleStringId);

    Page<User> searchAllCoMembers(String query, LoggedUser principal, Boolean small, Pageable pageable);

    Page<User> searchAllCoMembers(String query, List<String> roles, LoggedUser principal, Boolean small, Pageable pageable);
}