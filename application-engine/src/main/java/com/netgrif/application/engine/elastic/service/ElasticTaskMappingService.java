package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.objects.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import org.springframework.stereotype.Service;

@Service
public class ElasticTaskMappingService implements IElasticTaskMappingService {
    @Override
    public ElasticTask transform(Task task) {
        return new com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticTask(task);
    }
}
