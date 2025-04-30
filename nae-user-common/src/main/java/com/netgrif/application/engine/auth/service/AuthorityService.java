package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.Authority;

import java.util.List;

public interface AuthorityService {

    List<Authority> findAll();

    Authority getOrCreate(String name);

    Authority getOrCreatePermission(String name);

    Authority getOrCreateRole(String name);

    List<Authority> getAllPermissions();

    List<Authority> getAllRoles();

    Authority getOne(String id);

    List<Authority> findAllByIds(List<String> ids);
}
