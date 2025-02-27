package com.netgrif.application.engine.authentication.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Authority;

import java.util.List;

public interface IAuthorityService {

    List<Authority> findAll();

    Authority getOrCreate(String name);

    Authority getOrCreatePermission(String name);

    Authority getOrCreateRole(String name);

    List<Authority> getAllPermissions();

    List<Authority> getAllRoles();

    Authority getOne(String id);

    List<Authority> findAllByIds(List<String> ids);
}