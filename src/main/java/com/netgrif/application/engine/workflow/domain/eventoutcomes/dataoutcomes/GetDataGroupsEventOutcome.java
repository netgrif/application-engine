package com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Data;

import java.util.List;

@Data
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

    public List<DataGroup> getData() {
        return data;
    }

    public void setData(List<DataGroup> data) {
        this.data = data;
    }
}
