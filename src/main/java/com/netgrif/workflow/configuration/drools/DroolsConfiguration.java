package com.netgrif.workflow.configuration.drools;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.mail.interfaces.IMailService;
import com.netgrif.workflow.rules.domain.FactRepository;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.facts.QNetImportedFact;
import com.netgrif.workflow.rules.service.interfaces.IRuleEvaluationScheduleService;
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

    @Autowired
    private IRuleEvaluationScheduleService ruleEvaluationScheduleService;

    @Autowired
    @Qualifier("knowledgeBaseInitializer")
    private IKnowledgeBaseInitializer baseInitializer;

    @Autowired
    private FactRepository factRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private IElasticTaskService elasticTaskService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private IUserService userService;

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
        runtime.setGlobal("ruleEvaluationScheduleService", ruleEvaluationScheduleService);
        runtime.setGlobal("log", log);
    }

}
