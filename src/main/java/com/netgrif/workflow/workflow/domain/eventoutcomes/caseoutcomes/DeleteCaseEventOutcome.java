package com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

@Data
public class DeleteCaseEventOutcome extends EventOutcome {

    private String stringId;
}
