package com.netgrif.application.engine.auth.config;

import com.netgrif.application.engine.auth.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthBeansConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthorityService.class)
    public AuthorityService authorityService() {
        return new AuthorityServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(GroupService.class)
    public GroupService groupService() {
        return new GroupServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(RealmService.class)
    public RealmService realmService() {
        return new RealmServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(UserService.class)
    public UserService userService() {
        return new UserServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(PreferencesService.class)
    public PreferencesService preferencesService() {
        return new PreferencesServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(UserFactory.class)
    public UserFactory userFactory() {
        return new UserFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean(UserFactory.class)
    public AnonymousUserRefService anonymousUserRefService() {
        return new AnonymousUserRefServiceImpl();
    }
}
