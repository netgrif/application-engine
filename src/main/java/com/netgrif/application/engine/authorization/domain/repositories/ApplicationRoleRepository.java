package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ApplicationRole;

import java.util.List;

public interface ApplicationRoleRepository {

    boolean existsByImportId(String importId);

    ApplicationRole findByImportId(String importId);

    List<ApplicationRole> findAll();
}
