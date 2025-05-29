package com.netgrif.application.engine.configuration.authentication.providers.basic;

import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.configuration.EncryptionConfiguration;
import com.netgrif.application.engine.configuration.authentication.providers.NetgrifAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NetgrifBasicAuthenticationProviderLoader {

    private final EncryptionConfiguration passwordEncoder;
    private final IIdentityService identityService;

    @Lazy
    @Bean("netgrifBasicAuthenticationProvider")
    public NetgrifAuthenticationProvider netgrifBasicAuthenticationProvider() {
        NetgrifBasicAuthenticationProvider netgrifBasicAuthenticationProvider = new NetgrifBasicAuthenticationProvider(identityService);
        netgrifBasicAuthenticationProvider.setPasswordEncoder(passwordEncoder.bCryptPasswordEncoder());
        return netgrifBasicAuthenticationProvider;
    }

}
