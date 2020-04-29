package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.mail.IMailService;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DroolsConfiguration {

    private static final Logger log = LoggerFactory.getLogger("RuleEngine");
    private final IWorkflowService workflowService;
    private final ITaskService taskService;
    private final IElasticCaseService elasticCaseService;
    private final IElasticTaskService elasticTaskService;
    private final IDataService dataService;
    private final IMailService mailService;
    private final IUserService userService;

    @Autowired
    public DroolsConfiguration(IWorkflowService workflowService, ITaskService taskService, IElasticCaseService elasticCaseService, IElasticTaskService elasticTaskService, IDataService dataService, IMailService mailService, IUserService userService) {
        this.workflowService = workflowService;
        this.taskService = taskService;
        this.elasticCaseService = elasticCaseService;
        this.elasticTaskService = elasticTaskService;
        this.dataService = dataService;
        this.mailService = mailService;
        this.userService = userService;
    }

    @Bean
    public JPAKnowledgeService jpaKnowledgeService() {
    }

    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/workflow_service.drl"));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KieSession kieRuntime() {
        KieSession runtime = kieContainer().newKieSession();
        runtime.setGlobal("workflowService", workflowService);
        runtime.setGlobal("taskService", taskService);
        runtime.setGlobal("elasticCaseService", elasticCaseService);
        runtime.setGlobal("elasticTaskService", elasticTaskService);
        runtime.setGlobal("dataService", dataService);
        runtime.setGlobal("mailService", mailService);
        runtime.setGlobal("userService", userService);
        runtime.setGlobal("log", log);
        return runtime;
    }
}
