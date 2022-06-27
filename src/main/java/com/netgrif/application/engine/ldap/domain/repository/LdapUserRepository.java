package com.netgrif.application.engine.ldap.domain.repository;


import com.netgrif.application.engine.ldap.domain.LdapUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public interface LdapUserRepository extends MongoRepository<LdapUser, String>, QuerydslPredicateExecutor<LdapUser> {

    LdapUser findByDn(String dn);

    LdapUser findByEmail(String email);

    List<LdapUser> findAll();

    Page<LdapUser> findDistinctByDnIn(Iterable<String> dns, Pageable pageable);

}
