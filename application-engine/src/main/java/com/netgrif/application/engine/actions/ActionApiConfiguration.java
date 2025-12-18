package com.netgrif.application.engine.actions;

import com.netgrif.application.engine.adapter.spring.actions.ActionApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActionApiConfiguration {

    @Bean
    @ConditionalOnMissingBean(ActionApi.class)
    public ActionApi actionApi() {
        return new ActionApiImpl();
    }
}
