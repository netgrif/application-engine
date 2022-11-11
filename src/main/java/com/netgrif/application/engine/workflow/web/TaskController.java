package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.web.interfaces.ITaskController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/task")
@ConditionalOnProperty(
        value = "nae.task.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Task")
public class TaskController implements ITaskController {

    private final ITaskService taskService;

    private final IDataService dataService;

    private final IElasticTaskService elasticTaskService;

    private final IUserService userService;

    public TaskController(@Autowired ITaskService taskService,
                          @Autowired IDataService dataService,
                          @Autowired IElasticTaskService searchService,
                          @Autowired IUserService userService) {
        this.taskService = taskService;
        this.dataService = dataService;
        this.elasticTaskService = searchService;
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
    public ITaskService taskService() {
        return taskService;
    }

    @Override
    public IDataService dataService() {
        return dataService;
    }

    @Override
    public IElasticTaskService searchService() {
        return elasticTaskService;
    }
}
