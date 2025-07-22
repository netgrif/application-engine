package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;

import java.util.List;

public interface IBulkService {
    void bulkIndexCase(Case cases);
    void bulkIndexTasks();
    void bulkIndexTasks(List<Task> tasks);
    void indexCases();
}