package com.netgrif.application.engine.objects.event.events;

import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workspace.Workspaceable;
import lombok.Getter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class Event extends EventObject {
    @Serial
    private static final long serialVersionUID = -9102232475981679124L;

    protected static final String MISSING_IDENTIFIER = "NULL";

    private final EventPhase eventPhase;
    private final ActorRef actor;
    private final String workspaceId;
    private final Map<String, Attribute<?>> attributes;

    protected LocalDateTime time;

    protected Event() {
        this(null, null, null, null);
    }

    protected Event(Object source, ActorRef actor, String workspaceId) {
        this(source, null, actor, workspaceId);
    }

    protected Event(Object source, EventPhase eventPhase, ActorRef actor, String workspaceId) {
        super(source);
        this.eventPhase = eventPhase;
        this.workspaceId = workspaceId;
        this.actor = actor;
        this.time = LocalDateTime.now();
        this.attributes = new HashMap<>();
    }

    // todo javadoc
    protected static String getWorkspaceIdFromResource(Workspaceable resource) {
        return resource == null ? null : resource.getWorkspaceId();
    }

    public void addAttribute(String name, Attribute<?> attribute) {
        this.attributes.put(name, attribute);
    }

    public Attribute<?> getAttribute(String name) {
        return this.attributes.get(name);
    }

    public abstract String getMessage();
}
