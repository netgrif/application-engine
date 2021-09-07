package com.netgrif.workflow.ldap.service.interfaces;

import com.netgrif.workflow.ldap.domain.LdapGroupRef;

import java.util.List;


public interface ILdapGroupRefService {

    List<LdapGroupRef> getAllGroups();

    List<String> getAllGroupsCn();

    List<String> getAllGroupsDn();

}