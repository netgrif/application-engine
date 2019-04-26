package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.elastic.domain.ElasticTaskRepository;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ElasticTaskService implements IElasticTaskService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    @Autowired
    private ElasticTaskRepository repository;

    @Autowired
    private WorkflowService workflowService;

    @Async
    @Override
    public void index(Task task) {
        ElasticTask elasticCase = new ElasticTask(task);

        repository.save(elasticCase);

        log.info("[" + task.getCaseId() + "]: Task \"" + task.getTitle() + "\" ["+task.getStringId()+"] indexed");
    }
}