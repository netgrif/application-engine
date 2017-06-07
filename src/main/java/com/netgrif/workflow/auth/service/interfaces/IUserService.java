package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface IUserService {

    User save(User user);

    User saveNew(User user);

    User findById(Long id);

    List<User> findAll();

    Set<User> findByOrganizations(Set<Long> org);

    Set<User> findByProcessRoles(Set<String> roleIds);

    void assignAuthority(Long userId, Long authorityId);
}
