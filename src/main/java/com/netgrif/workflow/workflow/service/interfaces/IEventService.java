package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.events.DataEventType;
import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;

import java.util.List;
import java.util.Optional;

public interface IEventService {

    List<EventOutcome> runActions(List<Action> actions, Case useCase, Task task, Transition transition);

    List<EventOutcome> runActions(List<Action> actions, Case useCase, Optional<Task> task);

    List<EventOutcome> processDataEvents(Field field, DataEventType actionTrigger, EventPhase phase, Case useCase, Task task);

    List<EventOutcome> runEventActions(Case useCase, Task task, List<Action> actions, DataEventType trigger);

    void runEventActionsOnChanged(Task task, SetDataEventOutcome outcome, DataEventType trigger);

    ChangedFieldContainer mergeFieldsFromOutcomes(List<EventOutcome> outcomes, ChangedFieldContainer container, Task referencingTask);

    ChangedFieldContainer parseChangesFromOutcomes(List<EventOutcome> outcomes, ChangedFieldContainer container, Task referencedTask);
}
