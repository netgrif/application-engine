package com.netgrif.application.engine.authentication.service.interfaces;

import java.util.List;

public interface IAuthorityService {

    List<SessionRole> findAll();

    SessionRole getOrCreate(String name);

    SessionRole getOrCreatePermission(String name);

    SessionRole getOrCreateRole(String name);

    List<SessionRole> getAllPermissions();

    List<SessionRole> getAllRoles();

    SessionRole getOne(String id);

    List<SessionRole> findAllByIds(List<String> ids);
}