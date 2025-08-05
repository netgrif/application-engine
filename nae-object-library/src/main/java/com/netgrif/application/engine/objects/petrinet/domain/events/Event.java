package com.netgrif.application.engine.objects.petrinet.domain.events;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class Event extends BaseEvent {

    private EventType type;

    @Override
    public Event clone() {
        Event clone = new Event();
        clone.setId(this.getId());
        clone.setTitle(this.getTitle() == null ? null : this.getTitle().clone());
        clone.setMessage(this.getMessage() == null ? null : this.getMessage().clone());
        clone.setPreActions(this.getPreActions() == null ? null : getPreActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setPostActions(this.getPostActions() == null ? null : getPostActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setType(this.type);
        return clone;
    }
}
