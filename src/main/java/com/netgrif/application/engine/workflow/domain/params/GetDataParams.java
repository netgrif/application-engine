package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class GetDataParams {

    private String taskId;
    private Task task;
    private String actorId;
    private Case useCase;
    private Boolean isTransactional;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public GetDataParams(Task task, Case useCase, String actorId) {
        this.task = task;
        this.useCase = useCase;
        this.actorId = actorId;
    }

    public GetDataParams(String taskId, String actorId) {
        this.taskId = taskId;
        this.actorId = actorId;
    }
}
