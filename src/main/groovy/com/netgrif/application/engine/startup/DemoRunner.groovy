package com.netgrif.application.engine.startup

import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class DemoRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner)

    private ImportHelper helper
    private CaseRepository caseRepository
    private TaskRepository taskRepository
    private IElasticCaseService caseService
    private ElasticCaseRepository repository
    private ElasticTaskRepository elasticTaskRepository
    private IDataService dataService;
    private IWorkflowService workflowService;

    @Autowired
    void setHelper(ImportHelper helper) {
        this.helper = helper
    }

    @Autowired
    void setCaseRepository(CaseRepository caseRepository) {
        this.caseRepository = caseRepository
    }

    @Autowired
    void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository
    }

    @Autowired
    void setCaseService(IElasticCaseService caseService) {
        this.caseService = caseService
    }

    @Autowired
    void setRepository(ElasticCaseRepository repository) {
        this.repository = repository
    }

    @Autowired
    void setElasticTaskRepository(ElasticTaskRepository elasticTaskRepository) {
        this.elasticTaskRepository = elasticTaskRepository
    }

    @Autowired
    void setDataService(IDataService dataService) {
        this.dataService = dataService
    }

    @Autowired
    void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService
    }

    @Override
    void run(String... args) throws Exception {
        // Code what is written here DO NOT COMMIT!
    }
}
