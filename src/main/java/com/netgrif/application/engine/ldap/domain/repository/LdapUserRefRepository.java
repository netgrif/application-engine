package com.netgrif.application.engine.ldap.domain.repository;

import com.netgrif.application.engine.ldap.domain.LdapUserRef;
import com.querydsl.core.types.Predicate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public interface LdapUserRefRepository extends LdapRepository<LdapUserRef>, QuerydslPredicateExecutor<LdapUserRef> {

    LdapUserRef findByDn(String dn);

    LdapUserRef findByCn(String cn);

    List<LdapUserRef> findAll(Predicate predicate);

    List<LdapUserRef> findAll();

}
