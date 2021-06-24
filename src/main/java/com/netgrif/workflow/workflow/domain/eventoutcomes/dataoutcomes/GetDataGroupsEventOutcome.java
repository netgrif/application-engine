package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class GetDataGroupsEventOutcome extends EventOutcome {

    private List<DataGroup> data;

    public GetDataGroupsEventOutcome() {
    }

    public GetDataGroupsEventOutcome(I18nString message, List<DataGroup> data) {
        super(message);
        this.data = data;
    }

    public GetDataGroupsEventOutcome(I18nString message, List<EventOutcome> outcomes, List<DataGroup> data) {
        super(message, outcomes);
        this.data = data;
    }

    public GetDataGroupsEventOutcome(List<DataGroup> data) {
        this.data = data;
    }
}
