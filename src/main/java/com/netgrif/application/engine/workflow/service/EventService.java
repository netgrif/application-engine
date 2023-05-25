package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.petrinet.domain.DataFieldLogic;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Lazy
@Service
public class EventService implements IEventService {

    private final FieldActionsRunner actionsRunner;

    private final IWorkflowService workflowService;

    public EventService(FieldActionsRunner actionsRunner, IWorkflowService workflowService) {
        this.actionsRunner = actionsRunner;
        this.workflowService = workflowService;
    }

    @Override
    public List<EventOutcome> runActions(List<Action> actions, Case useCase, Task task, Transition transition) {
        log.info("[" + useCase.getStringId() + "]: Running actions of transition " + transition.getStringId());
        return runActions(actions, useCase, Optional.of(task));
    }

    @Override
    public List<EventOutcome> runActions(List<Action> actions) {
        return runActions(actions, null, Optional.empty());
    }

    @Override
    public List<EventOutcome> runActions(List<Action> actions, Case useCase, Optional<Task> task) {
        List<EventOutcome> allOutcomes = new ArrayList<>();
        if (actions.isEmpty()) {
            return allOutcomes;
        }
        actions.forEach(action -> {
            List<EventOutcome> outcomes = actionsRunner.run(action, useCase, task, useCase == null ? Collections.emptyList() : useCase.getPetriNet().getFunctions());
            outcomes.stream().filter(SetDataEventOutcome.class::isInstance)
                    .forEach(outcome -> {
                        if (((SetDataEventOutcome) outcome).getChangedFields().isEmpty()) return;
                        runEventActionsOnChanged(task.orElse(null), (SetDataEventOutcome) outcome, DataEventType.SET);
                    });
            allOutcomes.addAll(outcomes);
        });
        if (useCase != null) {
            workflowService.save(useCase);
        }
        return allOutcomes;
    }

    @Override
    public List<EventOutcome> runEventActions(Case useCase, Task task, List<Action> actions, DataEventType trigger) {
        List<EventOutcome> allOutcomes = new ArrayList<>();
        if (actions.isEmpty()) {
            return allOutcomes;
        }
        actions.forEach(action -> {
            List<EventOutcome> outcomes = actionsRunner.run(action, useCase, task == null ? Optional.empty() : Optional.of(task), useCase == null ? Collections.emptyList() : useCase.getPetriNet().getFunctions());
            outcomes.stream().filter(SetDataEventOutcome.class::isInstance)
                    .forEach(outcome -> {
                        if (((SetDataEventOutcome) outcome).getChangedFields().isEmpty()) return;
                        runEventActionsOnChanged(task, (SetDataEventOutcome) outcome, trigger);
                    });
            allOutcomes.addAll(outcomes);
        });
        return allOutcomes;
    }

    @Override
    public List<EventOutcome> processDataEvents(Field field, DataEventType actionTrigger, EventPhase phase, Case useCase, Task task) {
        LinkedList<Action> fieldActions = new LinkedList<>();
        if (field.getEvents() != null && field.getEvents().containsKey(actionTrigger)) {
            fieldActions.addAll(DataFieldLogic.getEventAction((DataEvent) field.getEvents().get(actionTrigger), phase));
        }
        if (task != null) {
            Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
            if (transition.getDataSet().containsKey(field.getStringId()) && !transition.getDataSet().get(field.getStringId()).getEvents().isEmpty()) {
                fieldActions.addAll(DataFieldLogic.getEventAction(transition.getDataSet().get(field.getStringId()).getEvents().get(actionTrigger), phase));
            }
        }

        if (fieldActions.isEmpty()) {
            return Collections.emptyList();
        }

        return runEventActions(useCase, task, fieldActions, actionTrigger);
    }

    @Override
    public void runEventActionsOnChanged(Task task, SetDataEventOutcome outcome, DataEventType trigger) {
        outcome.getChangedFields().forEach((s, changedField) -> {
            if (changedField.getAttributes().containsKey("value") && trigger == DataEventType.SET) {
                Field field = outcome.getCase().getField(s);
                log.info("[" + outcome.getCase().getStringId() + "] " + outcome.getCase().getTitle() + ": Running actions on changed field " + s);
                outcome.addOutcomes(processDataEvents(field, trigger, EventPhase.PRE, outcome.getCase(), outcome.getTask()));
                outcome.addOutcomes(processDataEvents(field, trigger, EventPhase.POST, outcome.getCase(), outcome.getTask()));
            }
        });
    }
}

