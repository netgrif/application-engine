package com.netgrif.application.engine.ldap.domain;

import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.odm.annotations.Id;

import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ConditionalOnExpression("${nae.ldap.enabled}")
public class LdapGroup {

    @Id
    private ObjectId id;

    private String dn;

    private String cn;

    private String description;

    private Set<ProcessRole> processesRoles;

}
