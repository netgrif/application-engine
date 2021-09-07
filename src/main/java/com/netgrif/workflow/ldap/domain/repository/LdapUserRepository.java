package com.netgrif.workflow.ldap.domain.repository;


import com.netgrif.workflow.auth.domain.ldapUser.LdapUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository

public interface LdapUserRepository extends MongoRepository<LdapUser, String>, QuerydslPredicateExecutor<LdapUser> {

    LdapUser findByDn(String dn);

    LdapUser findByEmail(String email);

    List<LdapUser> findAll();

    Page<LdapUser> findDistinctByDnIn(Iterable<String> dns, Pageable pageable);

}