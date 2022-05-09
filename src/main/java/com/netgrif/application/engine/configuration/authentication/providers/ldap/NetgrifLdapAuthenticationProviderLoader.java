package com.netgrif.application.engine.configuration.authentication.providers.ldap;

import com.netgrif.application.engine.auth.service.interfaces.ILdapUserRefService;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.ldap.service.LdapUserService;
import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Configuration
@ConditionalOnExpression("${nae.ldap.enabled}")
public class NetgrifLdapAuthenticationProviderLoader {

    @Autowired
    protected NaeLdapProperties ldapProperties;

    @Autowired
    private LdapUserService ldapUserService;

    @Autowired
    protected ILdapGroupRefService ldapGroupRefService;

    @Autowired
    protected ILdapUserRefService ldapUserRefService;

    @Lazy
    @Bean("netgrifLdapAuthenticationProvider")
    public NetgrifLdapAuthenticationProvider netgrifLdapAuthenticationProvider() {
        NetgrifLdapAuthenticationProvider netgrifLdapAuthenticationProvider = new NetgrifLdapAuthenticationProvider(ldapProperties);
        netgrifLdapAuthenticationProvider.setUserDetailsContextMapper(new UserDetailsContextMapperImpl(ldapUserService, ldapUserRefService, ldapGroupRefService, ldapProperties));
        return netgrifLdapAuthenticationProvider;
    }

}
