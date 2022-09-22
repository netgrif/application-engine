package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "nae.ldap")
public class NaeLdapProperties {

    private boolean enabled = false;

    private boolean ignorePartial = false;

    @Value("${spring.ldap.urls:#{\"\"}}")
    private String url;

    @Value("${spring.ldap.username:#{\"\"}}")
    private String username;

    @Value("${spring.ldap.password:#{\"\"}}")
    private String password;

    @Value("${spring.ldap.base:#{\"\"}}")
    private String base;

    private String superUsername;

    private String userFilter = "cn={0}";

    private String peopleSearchBase;

    private String[] peopleClass = {"inetOrgPerson", "person"};

    private String[] groupClass = {"groupOfNames"};

    private String groupSearchBase;

    private String mapCn = "cn";

    private String mapUid = "uid";

    private String mapMail = "uid";

    private String mapFirstName = "givenName";

    private String mapSurname = "sn";

    private String mapTelNumber = "telephoneNumber";

    private String mapDisplayName = "displayName";

    private String mapHomeDirectory = "homeDirectory";

    private String mapObjectClass = "objectClass";

    private String mapMemberOf = "MemberOf";

    private String mapUserPassword = "userPassword";

    private String mapGroupCn = "cn";

    private String mapGroupMember = "member";

    private String mapGroupObjectClass = "objectClass";

    private String mapGroupDescription = "description";

}
