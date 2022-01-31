package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.Authority;

import java.util.List;

public interface IAuthorityService {

    List<Authority> findAll();

    Authority getOrCreate(String name);

    Authority getOrCreatePermission(String name);

    Authority getOrCreateRole(String name);

    List<Authority> getAllPermissions();

    List<Authority> getAllRoles();

    Authority getOne(Long id);
}