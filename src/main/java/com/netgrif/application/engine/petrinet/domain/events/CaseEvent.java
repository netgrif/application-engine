package com.netgrif.application.engine.petrinet.domain.events;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.stream.Collectors;

@Data
public class CaseEvent extends BaseEvent {

    private CaseEventType type;

    @Override
    public CaseEvent clone() {
        CaseEvent clone = new CaseEvent();
        clone.setId(this.getId());
        clone.setTitle(this.getTitle().clone());
        clone.setMessage(this.getMessage().clone());
        clone.setPreActions(getPreActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setPreActions(getPostActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setType(this.type);
        return clone;
    }
}