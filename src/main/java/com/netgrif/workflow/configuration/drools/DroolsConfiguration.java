package com.netgrif.workflow.configuration.drools;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.mail.IMailService;
import com.netgrif.workflow.rules.domain.FactRepository;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DroolsConfiguration {

    private static final Logger log = LoggerFactory.getLogger("RuleEngine");
    private final IKnowledgeBaseInitializer baseInitializer;
    private final FactRepository factRepository;
    private final RuleRepository ruleRepository;
    private final IWorkflowService workflowService;
    private final ITaskService taskService;
    private final IElasticCaseService elasticCaseService;
    private final IElasticTaskService elasticTaskService;
    private final IDataService dataService;
    private final IMailService mailService;
    private final IUserService userService;

    @Autowired
    public DroolsConfiguration(@Qualifier("knowledgeBaseInitializer") IKnowledgeBaseInitializer baseInitializer, FactRepository factRepository, RuleRepository ruleRepository, IWorkflowService workflowService, ITaskService taskService, IElasticCaseService elasticCaseService, IElasticTaskService elasticTaskService, IDataService dataService, IMailService mailService, IUserService userService) {
        this.baseInitializer = baseInitializer;
        this.factRepository = factRepository;
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
        runtime.setGlobal("factRepository", factRepository);
        runtime.setGlobal("log", log);
    }

}
