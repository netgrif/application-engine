package com.netgrif.workflow.configuration.drools;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.configuration.drools.interfaces.IRuleEngineGlobalsProvider;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.mail.interfaces.IMailService;
import com.netgrif.workflow.rules.domain.FactRepository;
import com.netgrif.workflow.rules.service.interfaces.IRuleEvaluationScheduleService;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.netgrif.workflow.configuration.drools.RuleEngineGlobal.engineGlobal;

@Service
public class RuleEngineGlobalsProvider implements IRuleEngineGlobalsProvider {

    private static final Logger log = LoggerFactory.getLogger("RuleEngine");

    protected List<RuleEngineGlobal> globals;
    protected List<String> imports;

    private final IRuleEvaluationScheduleService ruleEvaluationScheduleService;
    private final IWorkflowService workflowService;
    private final ITaskService taskService;
    private final IElasticCaseService elasticCaseService;
    private final IElasticTaskService elasticTaskService;
    private final IDataService dataService;
    private final IMailService mailService;
    private final IUserService userService;
    private final FactRepository factRepository;

    public RuleEngineGlobalsProvider(@Autowired IRuleEvaluationScheduleService ruleEvaluationScheduleService,
                                     @Autowired IWorkflowService workflowService,
                                     @Autowired ITaskService taskService,
                                     @Autowired IElasticCaseService elasticCaseService,
                                     @Autowired IElasticTaskService elasticTaskService,
                                     @Autowired IDataService dataService,
                                     @Autowired IMailService mailService,
                                     @Autowired IUserService userService,
                                     @Autowired FactRepository factRepository) {
        this.ruleEvaluationScheduleService = ruleEvaluationScheduleService;
        this.workflowService = workflowService;
        this.taskService = taskService;
        this.elasticCaseService = elasticCaseService;
        this.elasticTaskService = elasticTaskService;
        this.dataService = dataService;
        this.mailService = mailService;
        this.userService = userService;
        this.factRepository = factRepository;
    }

    @PostConstruct
    void postConstruct() {
        this.globals = initializeGlobals();
        this.imports = initializeImports();
    }

    protected List<RuleEngineGlobal> initializeGlobals() {
        List<RuleEngineGlobal> globals = new ArrayList<>();
        globals.add(engineGlobal("com.netgrif.workflow.workflow.service.interfaces", "ITaskService", "taskService", taskService));
        globals.add(engineGlobal("com.netgrif.workflow.elastic.service.interfaces", "IElasticCaseService", "elasticCaseService", elasticCaseService));
        globals.add(engineGlobal("com.netgrif.workflow.elastic.service.interfaces", "IElasticTaskService", "elasticTaskService", elasticTaskService));
        globals.add(engineGlobal("com.netgrif.workflow.rules.domain", "FactRepository", "factRepository", factRepository));
        globals.add(engineGlobal("workflowService", workflowService));
        globals.add(engineGlobal("dataService", dataService));
        globals.add(engineGlobal("mailService", mailService));
        globals.add(engineGlobal("userService", userService));
        globals.add(engineGlobal("ruleEvaluationScheduleService", ruleEvaluationScheduleService));
        globals.add(engineGlobal("org.slf4j", "Logger", "log", log));
        return globals;
    }

    protected List<String> initializeImports() {
        List<String> imports = new ArrayList<>();
        globals.forEach(global -> imports.add(asImport(global.fullyQualifiedName())));
        return imports;
    }

    @Override
    public List<RuleEngineGlobal> globals() {
        return globals;
    }

    @Override
    public List<String> imports() {
        return imports;
    }

    @Override
    public void setGlobals(KieSession runtime) {
        globals.forEach(global -> runtime.setGlobal(global.getGlobalName(), global.getInjected()));
    }

    protected String asImport(String name) {
        return "import " + name + ";\n";
    }

}
