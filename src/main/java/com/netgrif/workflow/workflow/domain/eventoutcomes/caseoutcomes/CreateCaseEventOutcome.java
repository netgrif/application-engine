package com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

@Data
public class CreateCaseEventOutcome extends EventOutcome {

    private Case aCase;
}
