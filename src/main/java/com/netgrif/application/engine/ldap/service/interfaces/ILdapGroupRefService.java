package com.netgrif.application.engine.ldap.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.ldap.domain.LdapGroup;
import com.netgrif.application.engine.ldap.domain.LdapGroupRef;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.List;
import java.util.Set;

@ConditionalOnExpression("${nae.ldap.enabled}")
public interface ILdapGroupRefService {

    public List<LdapGroupRef> findAllGroups();

    public List<LdapGroup> getAllLdapGroupRoles();

    Set<ProcessRole> getProcessRoleByLdapGroup(Set<String> groupDn);

    public void addRoleToLdapGroup(String groupDn, Set<String> roleIds, LoggedUser loggedUser);

}
