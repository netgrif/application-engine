package com.netgrif.application.engine.workflow.domain.events;

import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.workflow.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.stream.Collectors;

@Data
public class ProcessEvent extends BaseEvent {

    private ProcessEventType type;

    @Override
    public ProcessEvent clone() {
        ProcessEvent clone = new ProcessEvent();
        clone.setId(this.getId());
        clone.setTitle(this.getTitle() == null ? null : this.getTitle().clone());
        clone.setMessage(this.getMessage() == null ? null : this.getMessage().clone());
        clone.setPreActions(this.getPreActions() == null ? null : getPreActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setPostActions(this.getPostActions() == null ? null : getPostActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setType(this.type);
        return clone;
    }
}

