package com.netgrif.application.engine.ldap.domain;

import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;


@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapGroup {

    @Id
    private ObjectId id;

    private String dn;

    private String cn;

    private String description;

    private Set<ProcessRole> processesRoles;

}
