package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.User;

import java.util.List;

public interface IUserService {
    void save(User user);

    User findByUsername(String username);

    User getLoggedInUser();

    List<User> findAll();
}
