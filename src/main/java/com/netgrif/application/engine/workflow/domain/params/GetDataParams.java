package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.auth.domain.IUser;
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
    private IUser user;
    private Case useCase;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public GetDataParams(Task task, Case useCase, IUser user) {
        this.task = task;
        this.useCase = useCase;
        this.user = user;
    }

    public GetDataParams(String taskId, IUser user) {
        this.taskId = taskId;
        this.user = user;
    }
}
