package com.netgrif.application.engine.ldap.domain.repository;

import com.netgrif.application.engine.ldap.domain.LdapGroup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public interface LdapGroupRoleRepository extends MongoRepository<LdapGroup, String> {

    LdapGroup findByDn(String dn);

    List<LdapGroup> findAllByDnIn(Iterable<String> dns);

}
