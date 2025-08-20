package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.service.RegistrationService;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImplBasedBeanConfiguration {

    @Bean
    @ConditionalOnMissingBean(IRegistrationService.class)
    public IRegistrationService registrationService() {
        return new RegistrationService();
    }
}
