package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

@Data
public class SetDataEventOutcome extends EventOutcome {

    private ChangedFieldsTree data;
}
