package com.netgrif.application.engine.workflow.params;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DelegateTaskParams {

    // todo javadoc

    private String taskId;
    private Task task;
    private Case useCase;
    private AbstractUser newAssignee;
    private String newAssigneeId;
    private AbstractUser delegator;
    private String delegatorId;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();
}
