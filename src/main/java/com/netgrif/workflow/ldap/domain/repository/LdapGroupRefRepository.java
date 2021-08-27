package com.netgrif.workflow.ldap.domain.repository;


import com.netgrif.workflow.ldap.domain.LdapGroupRef;
import com.querydsl.core.types.Predicate;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LdapGroupRefRepository extends LdapRepository<LdapGroupRef>, QuerydslPredicateExecutor<LdapGroupRef> {

    LdapGroupRef findByDn(String dn);

    LdapGroupRef findByCn(String cn);

    List<LdapGroupRef> findAll(Predicate predicate);

    List<LdapGroupRef> findAll();

}