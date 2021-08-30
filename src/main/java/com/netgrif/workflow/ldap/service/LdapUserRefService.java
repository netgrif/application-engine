package com.netgrif.workflow.ldap.service;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.configuration.properties.NaeLdapProperties;
import com.netgrif.workflow.event.events.user.UserRegistrationEvent;
import com.netgrif.workflow.ldap.domain.LdapConfiguration;
import com.netgrif.workflow.ldap.domain.LdapUser;
import com.netgrif.workflow.ldap.domain.LdapUserRef;
import com.netgrif.workflow.ldap.domain.repository.LdapUserRefRepository;
import com.netgrif.workflow.ldap.service.interfaces.ILdapUserRefService;
import com.netgrif.workflow.orgstructure.groups.config.GroupConfigurationProperties;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.stereotype.Service;

import javax.naming.Name;

@Service
@Slf4j
@ConditionalOnExpression("${nae.ldap.enabled}")
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
    private NaeLdapProperties ldapProperties;


    @Autowired
    private IUserService userService;

    @Autowired
    private LdapConfiguration ldapUserConfiguration;

    @Override
    public User createUser(LdapUserRef ldapUserRef) {
        LdapUser ldapUser = new LdapUser(ldapUserRef.getDn().toString(), ldapUserRef.getCn(), ldapUserRef.getUid(), ldapUserRef.getHomeDirectory(), ldapUserRef.getMail(), "password", ldapUserRef.getFirstName(), ldapUserRef.getSurname(), ldapUserRef.getTelNumber());
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
    public LdapUserRef findById(Name id) {
        DirContextOperations context
                = ldapUserConfiguration.ldapTemplate().lookupContext(id);
        LdapUserRef user = new LdapUserRef();
        user.setDn(context.getDn());
        user.setCn(verificationData(context, ldapProperties.getMapCn()));
        user.setUid(verificationData(context, ldapProperties.getMapUid()));
        user.setMail(verificationData(context, ldapProperties.getMapMail()));
        user.setFirstName(verificationData(context, ldapProperties.getMapFirstName()));
        user.setSurname(verificationData(context, ldapProperties.getMapSurname()));
        user.setFullName(verificationData(context, ldapProperties.getMapDisplayName()));
        user.setTelNumber(verificationData(context, ldapProperties.getMapTelNumber()));
        user.setHomeDirectory(verificationData(context, ldapProperties.getMapHomeDirectory()));

        return user;
    }

    private String verificationData(DirContextOperations context, String attribute) {
        if (attribute != null && !attribute.equals("")) {
                try {
                    return context.getStringAttribute(attribute);
                } catch (Exception e) {
                    log.warn("");
                    return null;
                }
        }
        return null;
    }


    @Override
    public User updateById(Name id, User savedUser) {
        DirContextOperations context = ldapUserConfiguration.ldapTemplate().lookupContext(id);
        LdapUserRef user = new LdapUserRef();
        user.setCn(verificationData(context, ldapProperties.getMapCn()));
        user.setUid(verificationData(context, ldapProperties.getMapUid()));
        user.setMail(verificationData(context, ldapProperties.getMapMail()));
        user.setFirstName(verificationData(context, ldapProperties.getMapFirstName()));
        user.setSurname(verificationData(context, ldapProperties.getMapSurname()));
        user.setTelNumber(verificationData(context, ldapProperties.getMapTelNumber()));
        user.setFullName(verificationData(context, ldapProperties.getMapDisplayName()));
        user.setHomeDirectory(verificationData(context, ldapProperties.getMapHomeDirectory()));
        LdapUser ldapUser = (LdapUser) savedUser;


        ldapUser.setCommonName(user.getCn());
        ldapUser.setUid(user.getUid());
        ldapUser.setHomeDirectory(user.getHomeDirectory());
        ldapUser.setEmail(user.getMail());
        ldapUser.setTelNumber(user.getTelNumber());
        ldapUser.setName(user.getFirstName());
        ldapUser.setSurname(user.getSurname());
        return ldapUserService.save(savedUser);
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