package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long>, QueryDslPredicateExecutor<Authority> {

    Authority findByName(String name);

    List<Authority> findAllByNameStartsWith(String prefix);
}