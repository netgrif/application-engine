package com.netgrif.application.engine.petrinet.domain.roles;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface RoleRepository extends MongoRepository<Role, String>, QuerydslPredicateExecutor<Role> {

    Set<Role> findAllByImportIdIn(Set<String> importIds);

    Set<Role> findAllByName_DefaultValue(String name);

    Set<Role> findAllByImportId(String importId);

    void deleteAllByIdIn(Collection<ObjectId> ids);

    boolean existsByImportId(String importId);
}