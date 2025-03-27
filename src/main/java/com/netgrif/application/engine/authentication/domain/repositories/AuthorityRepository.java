package com.netgrif.application.engine.authentication.domain.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorityRepository extends MongoRepository<SessionRole, String>, QuerydslPredicateExecutor<SessionRole> {

    SessionRole findByName(String name);

    List<SessionRole> findAllByNameStartsWith(String prefix);

    List<SessionRole> findAllByIdIn(List<ObjectId> ids);
}
