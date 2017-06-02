package com.fmworkflow.auth.service.interfaces;

import com.fmworkflow.auth.domain.Organization;
import com.fmworkflow.auth.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserService {

    User save(User user);

    User saveNew(User user);

    User findById(Long id);

    List<User> findAll();

    List<User> findByOrganizations(List<Long> org);

    List<User> findByProcessRole(String roleId);

    void assignRole(String userEmail, Long roleId);
}
