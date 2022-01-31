package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.workflow.domain.Task;

public interface IElasticTaskMappingService {
    ElasticTask transform(Task task);
}
