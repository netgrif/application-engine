package com.fmworkflow.auth.service.interfaces;

import com.fmworkflow.auth.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface IUserService {

    User save(User user);

    User saveNew(User user);

    User findById(Long id);

    List<User> findAll();

    List<User> findByOrganizations(Set<Long> org);

    List<User> findByProcessRole(String roleId);

    void assignRole(String userEmail, Long roleId);
}
