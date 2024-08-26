package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.auth.domain.IUser;
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
    private IUser user;
    @Builder.Default
    private boolean isTransactional = false;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public SetDataParams(Task task, DataSet dataSet, IUser user) {
        this.task = task;
        this.dataSet = dataSet;
        this.user = user;
    }

    public SetDataParams(String taskId, DataSet dataSet, IUser user) {
        this.taskId = taskId;
        this.dataSet = dataSet;
        this.user = user;
    }

    public SetDataParams(Case useCase, DataSet dataSet, IUser user) {
        this.useCase = useCase;
        this.dataSet = dataSet;
        this.user = user;
    }
}
