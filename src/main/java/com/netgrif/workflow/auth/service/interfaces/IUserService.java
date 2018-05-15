package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.orgstructure.domain.Member;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface IUserService {

    User save(User user);

    User saveNew(User user);

    Member upsertGroupMember(User user);

    User findById(Long id, boolean small);

    User findByEmail(String email, boolean small);

    List<User> findAll(boolean small);

    Set<User> findAllCoMembers(String email, boolean small);

    Set<User> findByProcessRoles(Set<String> roleIds, boolean small);

    void assignAuthority(Long userId, Long authorityId);

    void addDefaultRole(User user);

    void addDefaultAuthorities(User user);

    void encodeUserPassword(User user);

    LoggedUser getLoggedOrSystem();
}
