package com.netgrif.application.engine.ldap.service;


import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.UserState;
import com.netgrif.application.engine.auth.service.interfaces.ILdapUserRefService;
import com.netgrif.application.engine.configuration.ldap.LdapConfiguration;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.event.events.user.UserRegistrationEvent;
import com.netgrif.application.engine.ldap.domain.LdapUser;
import com.netgrif.application.engine.ldap.domain.LdapUserRef;
import com.netgrif.application.engine.ldap.domain.repository.LdapUserRefRepository;
import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import com.netgrif.application.engine.orgstructure.groups.config.GroupConfigurationProperties;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;


@Service
@Slf4j
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapUserRefService implements ILdapUserRefService {

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
    private LdapConfiguration ldapUserConfiguration;

    @Autowired
    protected ILdapGroupRefService ldapGroupRefService;

    @Override
    public IUser createUser(LdapUserRef ldapUserRef) {
        LdapUser ldapUser = new LdapUser(ldapUserRef.getDn().toString(), ldapUserRef.getCn(), ldapUserRef.getUid(), ldapUserRef.getHomeDirectory(), ldapUserRef.getMail(), ldapUserRef.getFirstName(), ldapUserRef.getSurname(), ldapUserRef.getMemberOf(), ldapUserRef.getTelNumber());
        ldapUser.setToken(null);
        ldapUser.setExpirationDate(null);
        ldapUser.setState(UserState.ACTIVE);

        String generatedString = RandomStringUtils.randomAlphanumeric(35);
        ldapUser.setPassword(generatedString);
        LdapUser savedUser = (LdapUser) ldapUserService.saveNew(ldapUser);
        savedUser.setNextGroups(this.groupService.getAllGroupsOfUser(savedUser));

        if (groupProperties.isDefaultEnabled())
            groupService.createGroup(savedUser);

        if (groupProperties.isSystemEnabled())
            groupService.addUserToDefaultGroup(savedUser);

        publisher.publishEvent(new UserRegistrationEvent(savedUser));

        savedUser.setPassword("n/a");
        return ldapUserService.save(savedUser);
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

        LdapQuery findAllGroupsGetMemberQuery =
                Arrays.stream(ldapProperties.getGroupClass()).map(it -> query().where("objectclass").is(it)).reduce(ContainerCriteria::and)
                        .orElse(query().where("objectclass").is(ldapProperties.getGroupClass()[0]))
                        .and((query().where(ldapProperties.getMapGroupMember()).is(
                                ((DirContextAdapter) context).getDn().toString() + "," +
                                        ldapUserConfiguration.contextSource().getBaseLdapPathAsString()
                        )));

        List<DirContextAdapter> ldapGroups = ldapUserConfiguration.ldapTemplate().search(findAllGroupsGetMemberQuery, (ContextMapper) ctx -> ((DirContextAdapter) ctx));

        user.setMemberOf(ldapGroups.stream().map(DirContextAdapter::getDn).map(Objects::toString).collect(Collectors.toSet()));

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
    public IUser updateById(Name id, IUser savedUser) {
        DirContextOperations context = ldapUserConfiguration.ldapTemplate().lookupContext(id);
        context.setAttributeValues("objectClass", ldapProperties.getPeopleClass());
        LdapUserRef user = new LdapUserRef();
        user.setUid(verificationData(context, ldapProperties.getMapUid()));
        user.setMail(verificationData(context, ldapProperties.getMapMail()));
        user.setFirstName(verificationData(context, ldapProperties.getMapFirstName()));
        user.setSurname(verificationData(context, ldapProperties.getMapSurname()));
        user.setTelNumber(verificationData(context, ldapProperties.getMapTelNumber()));
        user.setCn(verificationData(context, ldapProperties.getMapCn()));
        user.setFullName(verificationData(context, ldapProperties.getMapDisplayName()));
        LdapQuery findAllGroupsGetMemberQuery =
                Arrays.stream(ldapProperties.getGroupClass()).map(it -> query().where("objectclass").is(it)).reduce(ContainerCriteria::and)
                        .orElse(query().where("objectclass").is(ldapProperties.getGroupClass()[0]))
                        .and((query().where(ldapProperties.getMapGroupMember()).is(((DirContextAdapter) context).getDn().toString() + "," + ldapUserConfiguration.contextSource().getBaseLdapPathAsString())));
        List<DirContextAdapter> ldapGroups = ldapUserConfiguration.ldapTemplate().search(findAllGroupsGetMemberQuery, (ContextMapper) ctx -> ((DirContextAdapter) ctx));
        user.setMemberOf(ldapGroups.stream().map(DirContextAdapter::getDn).map(Objects::toString).collect(Collectors.toSet()));
        user.setHomeDirectory(verificationData(context, ldapProperties.getMapHomeDirectory()));
        LdapUser ldapUser = (LdapUser) savedUser;

        ldapUser.setCommonName(user.getCn());
        ldapUser.setUid(user.getUid());
        ldapUser.setHomeDirectory(user.getHomeDirectory());
        ldapUser.setEmail(user.getMail());
        ldapUser.setTelNumber(user.getTelNumber());
        ldapUser.setName(user.getFirstName());
        ldapUser.setMemberOf(user.getMemberOf());
        ldapUser.setSurname(user.getSurname());
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
