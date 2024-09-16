package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IEventService {

    List<EventOutcome> runActions(List<Action> actions, Case useCase, Task task, Transition transition, Map<String, String> params);

    List<EventOutcome> runActions(List<Action> actions, Case useCase, Optional<Task> task, Map<String, String> params);

    List<EventOutcome> runActions(List<Action> actions, Map<String, String> params);

    List<EventOutcome> processDataEvents(Field field, DataEventType actionTrigger, EventPhase phase, Case useCase, Task task, Map<String, String> params);

    List<EventOutcome> runEventActions(Case useCase, Task task, List<Action> actions, DataEventType trigger, Map<String, String> params);

    void runEventActionsOnChanged(Task task, SetDataEventOutcome outcome, DataEventType trigger);

    void runEventActionsOnChanged(Task task, SetDataEventOutcome outcome, DataEventType trigger, Map<String, String> params);
}
