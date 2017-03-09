package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.Role;

import java.util.List;

public interface IRoleService {
    List<Role> findAll();
}
