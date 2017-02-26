package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserService {
    void save(User user);

    User findByUsername(String username);

    User getLoggedInUser();

    List<User> findAll();
}
