package com.netgrif.application.engine.petrinet.domain.events;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.stream.Collectors;

@Data
public class Event extends BaseEvent {

    private EventType type;

    @Override
    public Event clone() {
        Event clone = new Event();
        clone.setId(this.getId());
        clone.setTitle(this.getTitle().clone());
        clone.setMessage(this.getMessage().clone());
        clone.setPreActions(getPreActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setPreActions(getPostActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setType(this.type);
        return clone;
    }
}