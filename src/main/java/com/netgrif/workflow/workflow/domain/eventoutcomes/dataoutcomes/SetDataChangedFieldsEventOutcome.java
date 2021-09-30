package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class SetDataChangedFieldsEventOutcome extends TaskEventOutcome {

    private ChangedFieldContainer data = new ChangedFieldContainer();

    public SetDataChangedFieldsEventOutcome() {
    }

    public SetDataChangedFieldsEventOutcome(SetDataEventOutcome outcome) {
        super(outcome.getMessage(), outcome.getOutcomes(), outcome.getACase(), outcome.getTask());
        this.data.putAll(outcome.getChangedFields());
    }

    public SetDataChangedFieldsEventOutcome(I18nString message, List<EventOutcome> outcomes, ChangedFieldContainer data, Case aCase, Task task) {
        super(message, outcomes, aCase, task);
        this.data = data;
    }

    public SetDataChangedFieldsEventOutcome(ChangedFieldContainer data) {
        this.data = data;
    }
}
