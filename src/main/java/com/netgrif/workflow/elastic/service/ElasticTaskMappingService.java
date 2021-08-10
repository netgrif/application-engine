package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.stereotype.Service;

@Service
public class ElasticTaskMappingService implements IElasticTaskMappingService {
    @Override
    public ElasticTask transform(Task task) {
        return new ElasticTask(task);
    }
}
