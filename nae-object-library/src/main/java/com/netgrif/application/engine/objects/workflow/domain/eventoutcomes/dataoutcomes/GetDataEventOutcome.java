package com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetDataEventOutcome extends TaskEventOutcome {

    private List<Field<?>> data;

    public GetDataEventOutcome(Case aCase, Task task) {
        super(aCase, task);
    }

    public GetDataEventOutcome(I18nString message, Case aCase, Task task) {
        super(message, aCase, task);
    }

    public GetDataEventOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase, Task task, List<Field<?>> data) {
        super(message, outcomes, aCase, task);
        this.data = data;
    }

}
