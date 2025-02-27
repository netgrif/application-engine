package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.authentication.web.responsebodies.UserResourceAssembler;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ExpressionDelegate;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ValidationDelegate;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
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

    @Bean("expressionDelegate")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ExpressionDelegate expressionDelegate() {
        return new ExpressionDelegate();
    }

    @Bean("validationDelegate")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ValidationDelegate validationDelegate() {
        return new ValidationDelegate();
    }

    @Bean("fileStorageConfiguration")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FileStorageConfiguration fileStorageConfiguration() {
        return new FileStorageConfiguration();
    }

    @Bean("userResourceAssembler")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public UserResourceAssembler userResourceAssembler() {
        return new UserResourceAssembler();
    }
}