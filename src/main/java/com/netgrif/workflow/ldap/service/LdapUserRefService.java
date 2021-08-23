package com.netgrif.workflow.ldap.service;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.user.UserRegistrationEvent;
import com.netgrif.workflow.ldap.domain.LdapUser;
import com.netgrif.workflow.ldap.domain.LdapUserRef;
import com.netgrif.workflow.ldap.domain.repository.LdapUserRefRepository;
import com.netgrif.workflow.ldap.service.interfaces.ILdapGroupRefService;
import com.netgrif.workflow.ldap.service.interfaces.ILdapUserRefService;
import com.netgrif.workflow.orgstructure.groups.config.GroupConfigurationProperties;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.ImportHelper;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class LdapUserRefService implements ILdapUserRefService {

    @Value("${spring.ldap.groups:null}")
    private String[] ldapSecurityGroups;

    @Autowired
    private LdapUserRefRepository repository;

    @Autowired
    private LdapUserService ldapUserService;

    @Autowired
    private INextGroupService groupService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private GroupConfigurationProperties groupProperties;


    @Autowired
    private IUserService userService;


    @Override
    public User createUser(LdapUserRef ldapUserRef) {
        LdapUser ldapUser = new LdapUser(ldapUserRef.getDn().toString(), ldapUserRef.getCn(), ldapUserRef.getUid(), ldapUserRef.getHomeDirectory(), ldapUserRef.getMail(), "password", ldapUserRef.getFirstName(), ldapUserRef.getSurname());
        ldapUser.setState(UserState.ACTIVE);
        User savedUser = ldapUserService.saveNew(ldapUser);
        ldapUser.setNextGroups(this.groupService.getAllGroupsOfUser(savedUser));

        if (groupProperties.isDefaultEnabled())
            groupService.createGroup(savedUser);

        if (groupProperties.isSystemEnabled())
            groupService.addUserToDefaultGroup(savedUser);

        savedUser.setGroups(savedUser.getGroups());
        userService.upsertGroupMember(savedUser);
        publisher.publishEvent(new UserRegistrationEvent(savedUser));

        return ldapUserService.save(ldapUser);
    }


    @Override
    public LdapUserRef findUserByDn(String dn) {
        return repository.findByDn(dn);
    }


    @Override
    public LdapUserRef findUserByCn(String cn) {
        return repository.findByCn(cn);
    }


}