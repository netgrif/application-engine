package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.interfaces.IWorkflowController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController()
@RequestMapping("/api/workflow")
@ConditionalOnProperty(
        value = "nae.case.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Workflow")
public class WorkflowController implements IWorkflowController {

    private final IWorkflowService workflowService;

    private final ITaskService taskService;

    private final IDataService dataService;

    private final IElasticCaseService elasticCaseService;

    private final IUserService userService;

    public WorkflowController(@Autowired IWorkflowService workflowService,
                              @Autowired ITaskService taskService,
                              @Autowired IDataService dataService,
                              @Autowired IElasticCaseService searchService,
                              @Autowired IUserService userService) {
        this.workflowService = workflowService;
        this.taskService = taskService;
        this.dataService = dataService;
        this.elasticCaseService = searchService;
        this.userService = userService;
    }

    @Override
    public IUserService userService() {
        return userService;
    }

    @Override
    public Logger log() {
        return log;
    }

    @Override
    public IWorkflowService workflowService() {
        return workflowService;
    }

    @Override
    public IElasticCaseService elasticCaseService() {
        return elasticCaseService;
    }

    @Override
    public ITaskService taskService() {
        return taskService;
    }

    @Override
    public IDataService dataService() {
        return dataService;
    }
}
