package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface IUserService {

    User save(User user);

    User saveNew(User user);

    User findById(Long id, boolean small);

    List<User> findAll(boolean small);

    Set<User> findByGroups(Set<Long> groups, boolean small);

    Set<User> findByProcessRoles(Set<String> roleIds, boolean small);

    void assignAuthority(Long userId, Long authorityId);
}
