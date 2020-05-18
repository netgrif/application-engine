package com.netgrif.workflow.configuration.drools;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.mail.IMailService;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import com.netgrif.workflow.rules.domain.facts.CaseRuleEvaluation;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.drools.model.Index;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.drools.model.DSL.*;
import static org.drools.model.FlowDSL.rule;

@Configuration
public class DroolsConfiguration {

    private static final Logger log = LoggerFactory.getLogger("RuleEngine");
    private final KnowledgeBaseInitializer baseInitializer;
    private final RuleRepository ruleRepository;
    private final IWorkflowService workflowService;
    private final ITaskService taskService;
    private final IElasticCaseService elasticCaseService;
    private final IElasticTaskService elasticTaskService;
    private final IDataService dataService;
    private final IMailService mailService;
    private final IUserService userService;

    @Autowired
    public DroolsConfiguration(KnowledgeBaseInitializer baseInitializer, RuleRepository ruleRepository, IWorkflowService workflowService, ITaskService taskService, IElasticCaseService elasticCaseService, IElasticTaskService elasticTaskService, IDataService dataService, IMailService mailService, IUserService userService) {
        this.baseInitializer = baseInitializer;
        this.ruleRepository = ruleRepository;
        this.workflowService = workflowService;
        this.taskService = taskService;
        this.elasticCaseService = elasticCaseService;
        this.elasticTaskService = elasticTaskService;
        this.dataService = dataService;
        this.mailService = mailService;
        this.userService = userService;
    }


    @Bean(name = "kieRuntime")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KieSession kieRuntime() {
        RefreshableKieBase baseWrapper = refreshableKieBase();
        if (baseWrapper.shouldRefresh()) {
            baseWrapper.refresh();
        }

        KieSession runtime = baseWrapper.kieBase().newKieSession();
        setKieSessionGlobals(runtime);
        return runtime;
    }

    @Bean
    public RefreshableKieBase refreshableKieBase() {
        return new RefreshableKieBase(ruleRepository, baseInitializer);
    }

    protected void setKieSessionGlobals(KieSession runtime) {
        runtime.setGlobal("workflowService", workflowService);
        runtime.setGlobal("taskService", taskService);
        runtime.setGlobal("elasticCaseService", elasticCaseService);
        runtime.setGlobal("elasticTaskService", elasticTaskService);
        runtime.setGlobal("dataService", dataService);
        runtime.setGlobal("mailService", mailService);
        runtime.setGlobal("userService", userService);
        runtime.setGlobal("log", log);
    }

}
