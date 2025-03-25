package com.netgrif.application.engine.ldap.service.interfaces;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.ldap.domain.LdapGroup;
import com.netgrif.application.engine.ldap.domain.LdapGroupRef;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.query.LdapQuery;

import java.util.List;
import java.util.Set;

@ConditionalOnExpression("${nae.ldap.enabled:false}")
public interface ILdapGroupRefService {

    List<LdapGroupRef> findAllGroups();

    List<LdapGroupRef> searchGroups(String searchText);

    List<LdapGroupRef> searchGroups(LdapQuery ldapQuery);

    List<LdapGroup> getAllLdapGroupRoles();

    void deleteRoleByPetriNet(String petriNet);

    void deleteRole(LdapGroup ldapGroup, String petriNet);

    Set<ProcessRole> getRoleByLdapGroup(Set<String> groupDn);

    void setRoleToLdapGroup(String groupDn, Set<String> roleIds, LoggedIdentity identity);

}
