package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ProcessRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String>, QuerydslPredicateExecutor<ProcessRole> {

    List<ProcessRole> findAllByImportIdIn(Set<String> importIds);

    List<ProcessRole> findAllByTitle_DefaultValue(String title);

    List<ProcessRole> findAllByImportId(String importId);

    boolean existsByImportId(String importId);

    ProcessRole findByImportId(String importId);
}