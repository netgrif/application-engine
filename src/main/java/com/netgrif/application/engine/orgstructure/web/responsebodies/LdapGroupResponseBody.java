package com.netgrif.application.engine.orgstructure.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.Set;

@Data
@AllArgsConstructor
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapGroupResponseBody {

    private String dn;

    private String cn;

    private String description;

    private Set<ProcessRole> processRoles;
}
