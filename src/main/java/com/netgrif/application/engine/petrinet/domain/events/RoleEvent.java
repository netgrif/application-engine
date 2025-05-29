package com.netgrif.application.engine.petrinet.domain.events;

import com.netgrif.application.engine.importer.model.RoleEventType;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleEvent extends BaseEvent {

    private RoleEventType type;

    @Override
    public RoleEvent clone() {
        RoleEvent clone = new RoleEvent();
        clone.setId(this.getId());
        clone.setTitle(this.getTitle() == null ? null : this.getTitle().clone());
        clone.setMessage(this.getMessage() == null ? null : this.getMessage().clone());
        clone.setPreActions(this.getPreActions() == null ? null : getPreActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setPostActions(this.getPostActions() == null ? null : getPostActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setType(this.type);
        return clone;
    }
}
