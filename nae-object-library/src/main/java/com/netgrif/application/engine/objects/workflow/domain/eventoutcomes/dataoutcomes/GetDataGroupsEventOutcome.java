package com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.application.engine.objects.petrinet.domain.DataGroup;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Data
@EqualsAndHashCode(callSuper = true)
public class GetDataGroupsEventOutcome extends TaskEventOutcome {

    private List<DataGroup> data;

    public GetDataGroupsEventOutcome(Case aCase, Task task) {
        super(aCase, task);
    }

    public GetDataGroupsEventOutcome(I18nString message, Case aCase, Task task) {
        super(message, aCase, task);
    }

    public GetDataGroupsEventOutcome(I18nString message, List<EventOutcome> outcomes, List<DataGroup> data, Case aCase, Task task) {
        super(message, outcomes, aCase, task);
        this.data = data;
    }

}
