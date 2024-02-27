package com.netgrif.application.engine.configuration.drools;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.drools.interfaces.IRuleEngineGlobalsProvider;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import com.netgrif.application.engine.rules.domain.FactRepository;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEvaluationScheduleService;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.netgrif.application.engine.configuration.drools.RuleEngineGlobal.engineGlobal;

@Service
public class RuleEngineGlobalsProvider implements IRuleEngineGlobalsProvider {

    private static final Logger log = LoggerFactory.getLogger("RuleEngine");

    protected List<RuleEngineGlobal> globals;
    protected List<String> imports;

    private IRuleEvaluationScheduleService ruleEvaluationScheduleService;
    private IWorkflowService workflowService;
    private ITaskService taskService;
    private IElasticCaseService elasticCaseService;
    private IElasticTaskService elasticTaskService;
    private IDataService dataService;
    private IMailService mailService;
    private IUserService userService;
    private FactRepository factRepository;

    @Autowired
    public void setRuleEvaluationScheduleService(IRuleEvaluationScheduleService ruleEvaluationScheduleService) {
        this.ruleEvaluationScheduleService = ruleEvaluationScheduleService;
    }

    @Autowired
    public void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Autowired
    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setElasticCaseService(IElasticCaseService elasticCaseService) {
        this.elasticCaseService = elasticCaseService;
    }

    @Autowired
    public void setElasticTaskService(IElasticTaskService elasticTaskService) {
        this.elasticTaskService = elasticTaskService;
    }

    @Autowired
    public void setDataService(IDataService dataService) {
        this.dataService = dataService;
    }

    @Autowired
    public void setMailService(IMailService mailService) {
        this.mailService = mailService;
    }

    @Autowired
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setFactRepository(FactRepository factRepository) {
        this.factRepository = factRepository;
    }

    @PostConstruct
    void postConstruct() {
        this.globals = initializeGlobals();
        this.imports = initializeImports();
    }

    protected List<RuleEngineGlobal> initializeGlobals() {
        List<RuleEngineGlobal> globals = new ArrayList<>();
        globals.add(engineGlobal("com.netgrif.application.engine.workflow.service.interfaces", "ITaskService", "taskService", taskService));
        globals.add(engineGlobal("com.netgrif.application.engine.auth.service.interfaces", "IUserService", "userService", userService));
        globals.add(engineGlobal("com.netgrif.application.engine.elastic.service.interfaces", "IElasticCaseService", "elasticCaseService", elasticCaseService));
        globals.add(engineGlobal("com.netgrif.application.engine.elastic.service.interfaces", "IElasticTaskService", "elasticTaskService", elasticTaskService));
        globals.add(engineGlobal("com.netgrif.application.engine.rules.domain", "FactRepository", "factRepository", factRepository));
        globals.add(engineGlobal("workflowService", workflowService));
        globals.add(engineGlobal("dataService", dataService));
        globals.add(engineGlobal("mailService", mailService));
        globals.add(engineGlobal("ruleEvaluationScheduleService", ruleEvaluationScheduleService));
        globals.add(engineGlobal("org.slf4j", "Logger", "log", log));
        return globals;
    }

    protected List<String> initializeImports() {
        List<String> imports = new ArrayList<>();
        globals.forEach(global -> imports.add(asImport(global.fullyQualifiedName())));
        imports.add(asImport("com.netgrif.application.engine.petrinet.domain.events.*"));
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
