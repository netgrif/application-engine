package com.netgrif.application.engine.objects.event.events;

import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workspace.Workspaceable;
import lombok.Getter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.EventObject;

@Getter
public abstract class Event extends EventObject {
    @Serial
    private static final long serialVersionUID = -9102232475981679124L;
    private final EventPhase eventPhase;
    private final String workspaceId;
    protected LocalDateTime time;

    protected static final String MISSING_IDENTIFIER = "NULL";

    protected Event() {
        this(null, null, null);
    }

    protected Event(Object source, String workspaceId) {
        this(source, null, workspaceId);
    }

    protected Event(Object source, EventPhase eventPhase, String workspaceId) {
        super(source);
        this.eventPhase = eventPhase;
        this.workspaceId = workspaceId;
        this.time = LocalDateTime.now();
    }

    // todo javadoc
    protected static String getWorkspaceIdFromResource(Workspaceable resource) {
        return resource == null ? null : resource.getWorkspaceId();
    }

    public abstract String getMessage();
}
