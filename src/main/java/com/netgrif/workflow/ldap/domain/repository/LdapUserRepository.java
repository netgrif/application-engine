package com.netgrif.workflow.ldap.domain.repository;


import com.netgrif.workflow.ldap.domain.LdapUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository

public interface LdapUserRepository extends JpaRepository<LdapUser, Long>, QuerydslPredicateExecutor<LdapUser> {

    LdapUser findByDn(String dn);

    LdapUser findByEmail(String email);

    List<LdapUser> findAll();

    Page<LdapUser> findDistinctByDnIn(Iterable<String> dns, Pageable pageable);

}