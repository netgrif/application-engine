package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.importer.model.DataEventType;
import com.netgrif.application.engine.workflow.domain.DataRef;
import com.netgrif.application.engine.workflow.domain.Transition;
import com.netgrif.application.engine.workflow.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.workflow.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Lazy
@Service
public class EventService implements IEventService {

    private final ActionRunner actionsRunner;

    public EventService(ActionRunner actionsRunner) {
        this.actionsRunner = actionsRunner;
    }

    @Override
    public List<EventOutcome> runActions(List<Action> actions, Case useCase, Task task, Transition transition, Map<String, String> params) {
        log.info("[{}]: Running actions of transition {}", useCase.getStringId(), transition.getStringId());
        return runActions(actions, useCase, Optional.of(task), params);
    }

    @Override
    public List<EventOutcome> runActions(List<Action> actions, Map<String, String> params) {
        return runActions(actions, null, Optional.empty(), params);
    }

    @Override
    public List<EventOutcome> runActions(List<Action> actions, Case useCase, Optional<Task> task, Map<String, String> params) {
        List<EventOutcome> allOutcomes = new ArrayList<>();
        actions.forEach(action -> {
            List<EventOutcome> outcomes = actionsRunner.run(action, useCase, task, null, params, useCase == null ? Collections.emptyList() : useCase.getProcess().getFunctions());
            outcomes.stream().filter(SetDataEventOutcome.class::isInstance)
                    .forEach(outcome -> {
                        if (((SetDataEventOutcome) outcome).getChangedFields().getFields().isEmpty()) {
                            return;
                        }
                        runEventActionsOnChanged(task.orElse(null), (SetDataEventOutcome) outcome, DataEventType.SET, params);
                    });
            allOutcomes.addAll(outcomes);
        });
        return allOutcomes;
    }

    @Override
    public List<EventOutcome> runEventActions(Case useCase, Task task, Field<?> newDataField, List<Action> actions, DataEventType trigger, Map<String, String> params) {
        List<EventOutcome> allOutcomes = new ArrayList<>();
        actions.stream()
                .filter(a -> a.getSetDataType().isTriggered(newDataField))
                .forEach(action -> {
                    List<EventOutcome> outcomes = actionsRunner.run(action, useCase, task == null ? Optional.empty() : Optional.of(task), newDataField, params, useCase == null ? Collections.emptyList() : useCase.getProcess().getFunctions());
                    outcomes.stream()
                            .filter(SetDataEventOutcome.class::isInstance)
                            .filter(outcome -> !((SetDataEventOutcome) outcome).getChangedFields().getFields().isEmpty())
                            .forEach(outcome -> runEventActionsOnChanged(task, (SetDataEventOutcome) outcome, trigger, params));
                    allOutcomes.addAll(outcomes);
                });
        return allOutcomes;
    }

    @Override
    public List<EventOutcome> processDataEvents(Field<?> field, DataEventType actionTrigger, EventPhase phase, Case useCase, Task task, Field<?> newDataField, Map<String, String> params) {
        LinkedList<Action> fieldActions = new LinkedList<>();
        if (field.getEvents() != null && field.getEvents().containsKey(actionTrigger)) {
            fieldActions.addAll(DataRef.getEventAction(field.getEvents().get(actionTrigger), phase));
        }
        if (task != null) {
            Transition transition = useCase.getProcess().getTransition(task.getTransitionId());
            if (transition.getDataSet().containsKey(field.getStringId()) && !transition.getDataSet().get(field.getStringId()).getEvents().isEmpty()) {
                fieldActions.addAll(DataRef.getEventAction(transition.getDataSet().get(field.getStringId()).getEvents().get(actionTrigger), phase));
            }
        }

        if (fieldActions.isEmpty()) {
            return Collections.emptyList();
        }
        return runEventActions(useCase, task, newDataField, fieldActions, actionTrigger, params);
    }

    @Override
    public void runEventActionsOnChanged(Task task, SetDataEventOutcome outcome, DataEventType trigger) {
        runEventActionsOnChanged(task, outcome, trigger, new HashMap<>());
    }

    @Override
    public void runEventActionsOnChanged(Task task, SetDataEventOutcome outcome, DataEventType trigger, Map<String, String> params) {
        // TODO: release/8.0.0 6.2.5
        if (trigger != DataEventType.SET) {
            return;
        }
        outcome.getChangedFields().getFields().entrySet().stream()
                .filter(entry -> entry.getValue().getRawValue() != null)
                .forEach(entry -> {
                    String fieldId = entry.getKey();
                    Field<?> field = outcome.getCase().getDataSet().get(fieldId);
                    log.info("[" + outcome.getCase().getStringId() + "] " + outcome.getCase().getTitle() + ": Running actions on changed field " + fieldId);
                    // TODO: release/8.0.0 changed fields
                    outcome.addOutcomes(processDataEvents(field, trigger, EventPhase.PRE, outcome.getCase(), outcome.getTask(), null, params));
                    outcome.addOutcomes(processDataEvents(field, trigger, EventPhase.POST, outcome.getCase(), outcome.getTask(), null, params));
                });
    }
}

