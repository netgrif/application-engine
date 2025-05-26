package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class SetDataParams {

    private Task task;
    private String taskId;
    private Case useCase;
    private DataSet dataSet;
    private String actorId;
    private Boolean isTransactional;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public SetDataParams(Task task, DataSet dataSet, String actorId) {
        this.task = task;
        this.dataSet = dataSet;
        this.actorId = actorId;
    }

    public SetDataParams(String taskId, DataSet dataSet, String actorId) {
        this.taskId = taskId;
        this.dataSet = dataSet;
        this.actorId = actorId;
    }

    public SetDataParams(Case useCase, DataSet dataSet, String actorId) {
        this.useCase = useCase;
        this.dataSet = dataSet;
        this.actorId = actorId;
    }
}
