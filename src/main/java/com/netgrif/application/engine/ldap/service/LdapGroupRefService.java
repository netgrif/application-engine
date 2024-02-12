package com.netgrif.application.engine.ldap.service;


import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.configuration.ldap.LdapConfiguration;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.ldap.domain.LdapGroup;
import com.netgrif.application.engine.ldap.domain.LdapGroupRef;
import com.netgrif.application.engine.ldap.domain.repository.LdapGroupRoleRepository;
import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import lombok.extern.slf4j.Slf4j;
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
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapGroupRefService implements ILdapGroupRefService {

    private final LdapConfiguration ldapConfiguration;

    private final LdapGroupRoleRepository ldapGroupRoleRepository;

    private final IProcessRoleService processRoleService;

    private final NaeLdapProperties ldapProperties;

    public LdapGroupRefService(LdapConfiguration ldapConfiguration, LdapGroupRoleRepository ldapGroupRoleRepository,
                               IProcessRoleService processRoleService, NaeLdapProperties ldapProperties) {
        this.ldapConfiguration = ldapConfiguration;
        this.ldapGroupRoleRepository = ldapGroupRoleRepository;
        this.processRoleService = processRoleService;
        this.ldapProperties = ldapProperties;
    }

    @Override
    public List<LdapGroupRef> findAllGroups() {
        LdapQuery findAllGroupsQuery = Arrays.stream(ldapProperties.getGroupClass()).map(it -> query().where(ldapProperties.getMapGroupObjectClass()).is(it)).reduce(ContainerCriteria::and).orElse(query().where(ldapProperties.getMapGroupObjectClass()).is(ldapProperties.getGroupClass()[0]));
        return searchGroups(findAllGroupsQuery);
    }

    @Override
    public List<LdapGroupRef> searchGroups(String fulltext) {
        LdapQuery searchQuerry = Arrays.stream(ldapProperties.getGroupClass()).map(it -> query().where(ldapProperties.getMapGroupObjectClass()).is(it)).reduce(ContainerCriteria::and).orElse(query().where(ldapProperties.getMapGroupObjectClass()).is(ldapProperties.getGroupClass()[0]))
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
    public void deleteProcessRoleByPetrinet(String petriNet) {
        ldapGroupRoleRepository.findAll().stream()
                .filter(ldapGroup -> ldapGroup.getProcessesRoles().stream().anyMatch(processRole -> processRole.getNetId().equals(petriNet)))
                .forEach(it -> deleteProcessRole(it, petriNet));
    }

    @Override
    public void deleteProcessRole(LdapGroup ldapGroup, String petriNet) {
        Set<ProcessRole> processRoles = ldapGroup.getProcessesRoles();
        processRoles.forEach(it -> {
            if (it.getNetId().equals(petriNet)) {
                processRoles.remove(it);
            }
        });
        ldapGroup.setProcessesRoles(processRoles);
        ldapGroupRoleRepository.save(ldapGroup);
    }

    @Override
    public Set<ProcessRole> getProcessRoleByLdapGroup(Set<String> groupDn) {
        return ldapGroupRoleRepository.findAllByDnIn(groupDn).stream().map(LdapGroup::getProcessesRoles).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public void setRoleToLdapGroup(String groupDn, Set<String> requestedRolesIds, LoggedUser loggedUser) {
        Set<ProcessRole> requestedRoles = processRoleService.findByIds(requestedRolesIds);
        if (requestedRoles.isEmpty() && !requestedRolesIds.isEmpty())
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

