package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.workflow.domain.Task;
import org.springframework.stereotype.Service;

@Service
public class ElasticTaskMappingService implements IElasticTaskMappingService {
    @Override
    public ElasticTask transform(Task task) {
        return new ElasticTask(task);
    }
}
