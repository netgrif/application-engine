package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.core.elastic.domain.ElasticTask;
import com.netgrif.core.workflow.domain.Task;

public interface IElasticTaskMappingService {
    ElasticTask transform(Task task);
}
