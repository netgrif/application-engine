package com.netgrif.application.engine.configuration.drools;

import com.netgrif.application.engine.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.application.engine.configuration.drools.interfaces.IRefreshableKieBase;
import com.netgrif.application.engine.configuration.drools.interfaces.IRuleEngineGlobalsProvider;
import com.netgrif.application.engine.rules.domain.RuleRepository;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DroolsConfiguration {

    private IKnowledgeBaseInitializer baseInitializer;

    private IRuleEngineGlobalsProvider sessionInitializer;

    private RuleRepository ruleRepository;

    @Autowired
    public void setBaseInitializer(IKnowledgeBaseInitializer baseInitializer) {
        this.baseInitializer = baseInitializer;
    }

    @Autowired
    public void setSessionInitializer(IRuleEngineGlobalsProvider sessionInitializer) {
        this.sessionInitializer = sessionInitializer;
    }

    @Autowired
    public void setRuleRepository(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Bean(name = "kieRuntime")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KieSession kieRuntime() {
        IRefreshableKieBase baseWrapper = refreshableKieBase();
        if (baseWrapper.shouldRefresh()) {
            baseWrapper.refresh();
        }

        KieSession runtime = baseWrapper.kieBase().newKieSession();
        sessionInitializer.setGlobals(runtime);
        return runtime;
    }

    @Bean
    public IRefreshableKieBase refreshableKieBase() {
        return new RefreshableKieBase(ruleRepository, baseInitializer);
    }

    @Bean
    public ObjectDataCompiler objectDataCompiler() {
        return new ObjectDataCompiler();
    }
}
