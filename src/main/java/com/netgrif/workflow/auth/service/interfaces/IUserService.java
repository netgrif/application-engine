package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.orgstructure.domain.Group;
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

//    Set<User> findByOrganizations(Set<Long> org, boolean small);

    Set<User> findByProcessRoles(Set<String> roleIds, boolean small);

    void assignAuthority(Long userId, Long authorityId);

//    List<Group> getAllOrganizations();
}
