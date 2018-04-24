package com.netgrif.workflow.configuration;

import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PrototypesConfiguration {

    @Bean("importer")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Importer importer() {
        return new Importer();
    }

    @Bean("actionDelegate")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ActionDelegate actionDelegate() {
        return new ActionDelegate();
    }
}