package com.netgrif.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "nae.ldap")
public class NaeLdapProperties {

    private boolean enabled = false;

    private String superUsername;

    private String userFilter;

    private String base;

    private String peopleSearchBase;

    private String peopleClass;

    private String groupSearchBase;

    private String mapCn = "cn";

    private String mapUid = "uid";

    private String mapMail = "uid";

    private String mapFirstName = "givenName";

    private String mapSurname = "sn";

    private String mapDisplayName = "displayName";

    private String mapHomeDirectory = "homeDirectory";

    private String mapObjectClass = "objectClass";

    private String mapMemberOf = "MemberOf";

    private String mapUserPassword = "userPassword";

}

