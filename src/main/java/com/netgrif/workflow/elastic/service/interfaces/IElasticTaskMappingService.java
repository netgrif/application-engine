package com.netgrif.workflow.elastic.service.interfaces;

import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.workflow.domain.Task;

public interface IElasticTaskMappingService {
    ElasticTask transform(Task task);
}
