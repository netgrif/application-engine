package com.netgrif.application.engine.orgstructure.web.requestbodies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LdapGroupRoleAssignRequestBody {

    private String groupDn;

    private Set<String> roleIds;
}
