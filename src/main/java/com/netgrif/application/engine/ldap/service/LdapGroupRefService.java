package com.netgrif.application.engine.ldap.service;


import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.configuration.ldap.LdapConfiguration;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.ldap.domain.LdapGroup;
import com.netgrif.application.engine.ldap.domain.LdapGroupRef;
import com.netgrif.application.engine.ldap.domain.repository.LdapGroupRoleRepository;
import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Slf4j
@Service
@ConditionalOnExpression("${nae.ldap.enabled}")
public class LdapGroupRefService implements ILdapGroupRefService {

    @Autowired
    private LdapConfiguration ldapConfiguration;

    @Autowired
    private LdapGroupRoleRepository ldapGroupRoleRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private NaeLdapProperties ldapProperties;

    @Override
    public List<LdapGroupRef> findAllGroups() {
        LdapQuery findAllGroupsQuery = Arrays.stream(ldapProperties.getGroupClass()).map(it -> query().where("objectclass").is(it)).reduce(ContainerCriteria::and).orElse(query().where("objectclass").is(ldapProperties.getGroupClass()[0]));
        return searchGroups(findAllGroupsQuery);
    }

    @Override
    public List<LdapGroupRef> searchGroups(String fulltext) {
        LdapQuery searchQuerry = Arrays.stream(ldapProperties.getGroupClass()).map(it -> query().where("objectclass").is(it)).reduce(ContainerCriteria::and).orElse(query().where("objectclass").is(ldapProperties.getGroupClass()[0]))
                .and(query().where(ldapProperties.getMapGroupCn()).whitespaceWildcardsLike(fulltext).or(query().where(ldapProperties.getMapGroupDescription()).whitespaceWildcardsLike(fulltext)));
        return searchGroups(searchQuerry);
    }

    @Override
    public List<LdapGroupRef> searchGroups(LdapQuery ldapQuery) {
        List<DirContextAdapter> ldapGroups = ldapConfiguration.ldapTemplate().search(ldapQuery, (ContextMapper) ctx -> ((DirContextAdapter) ctx));
        return ldapGroups.stream()
                .map(ldapGroup -> new LdapGroupRef(
                        ldapGroup.getDn(),
                        ldapGroup.getStringAttribute(ldapProperties.getMapGroupCn()),
                        ldapGroup.getStringAttributes(ldapProperties.getMapGroupMember()),
                        ldapGroup.getStringAttributes(ldapProperties.getMapGroupObjectClass()),
                        ldapGroup.getStringAttribute(ldapProperties.getMapGroupDescription()))
                ).collect(Collectors.toList());
    }

    @Override
    public List<LdapGroup> getAllLdapGroupRoles() {
        return ldapGroupRoleRepository.findAll();
    }

    @Override
    public Set<ProcessRole> getProcessRoleByLdapGroup(Set<String> groupDn) {
        return ldapGroupRoleRepository.findAllByDnIn(groupDn).stream().map(LdapGroup::getProcessesRoles).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public void addRoleToLdapGroup(String groupDn, Set<String> requestedRolesIds, LoggedUser loggedUser) {
        Set<ProcessRole> requestedRoles = processRoleRepository.findAllBy_idIn(requestedRolesIds);
        if (requestedRoles.isEmpty() && requestedRolesIds.size() != 0)
            throw new IllegalArgumentException("No process roles found.");
        if (requestedRoles.size() != requestedRolesIds.size())
            throw new IllegalArgumentException("Not all process roles were found!");

        LdapGroup ldapGroup = ldapGroupRoleRepository.findByDn(groupDn);
        if (ldapGroup == null) {
            LdapGroup newLdapGroup = new LdapGroup();
            newLdapGroup.setDn(groupDn);
            newLdapGroup.setProcessesRoles(requestedRoles);
            ldapGroupRoleRepository.save(newLdapGroup);
        } else {
            ldapGroup.setProcessesRoles(requestedRoles);
            ldapGroupRoleRepository.save(ldapGroup);
        }

    }
}

