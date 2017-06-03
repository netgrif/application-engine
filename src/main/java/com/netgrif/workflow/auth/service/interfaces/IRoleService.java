package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.Role;

import java.util.List;

public interface IRoleService {
    List<Role> findAll();
}
