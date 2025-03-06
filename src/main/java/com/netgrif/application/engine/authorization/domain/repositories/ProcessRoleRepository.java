package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ProcessRole;

import java.util.List;
import java.util.Set;

public interface ProcessRoleRepository {

    List<ProcessRole> findAllByImportIdIn(Set<String> importIds);

    List<ProcessRole> findAllByTitle_DefaultValue(String title);

    List<ProcessRole> findAllByImportId(String importId);

    boolean existsByImportId(String importId);

    ProcessRole findByImportId(String importId);

    List<ProcessRole> findAll();
}