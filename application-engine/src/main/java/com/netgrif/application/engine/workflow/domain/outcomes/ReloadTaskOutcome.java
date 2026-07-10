package com.netgrif.application.engine.workflow.domain.outcomes;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReloadTaskOutcome {
    private boolean anyTaskExecuted;
    private boolean useCaseSaved;
}
