package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorityRepository extends MongoRepository<Authority, String>, QuerydslPredicateExecutor<Authority> {

    Authority findByName(String name);

    List<Authority> findAllByNameStartsWith(String prefix);
}