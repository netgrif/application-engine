package com.netgrif.application.engine.workflow.domain.outcomes;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoEventTaskOutcome {

    private Task task;
    private Case useCase;
}
