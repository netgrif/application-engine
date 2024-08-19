package com.netgrif.application.engine.ldap.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.ldap.domain.LdapGroup;
import com.netgrif.application.engine.ldap.domain.LdapGroupRef;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.query.LdapQuery;

import java.util.List;
import java.util.Set;

@ConditionalOnExpression("${nae.ldap.enabled:false}")
public interface ILdapGroupRefService {

    public List<LdapGroupRef> findAllGroups();

    public List<LdapGroupRef> searchGroups(String searchText);

    List<LdapGroupRef> searchGroups(LdapQuery ldapQuery);

    public List<LdapGroup> getAllLdapGroupRoles();

    void deleteProcessRoleByPetrinet(String petrinet);

    void deleteProcessRole(LdapGroup ldapGroup, String petriNet);

    Set<ProcessRole> getProcessRoleByLdapGroup(Set<String> groupDn);

    public void setRoleToLdapGroup(String groupDn, Set<String> roleIds, LoggedUser loggedUser);

}
