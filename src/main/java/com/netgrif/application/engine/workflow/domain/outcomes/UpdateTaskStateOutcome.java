package com.netgrif.application.engine.workflow.domain.outcomes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateTaskStateOutcome {

    private boolean wasChanged;
    private boolean mustBeExecuted;
}
