package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.petrinet.domain.DataFieldLogic;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.logic.CaseChangedFields;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.workflow.petrinet.domain.events.DataEvent;
import com.netgrif.workflow.petrinet.domain.events.DataEventType;
import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.workflow.workflow.service.interfaces.IEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class EventService implements IEventService {

    private final FieldActionsRunner actionsRunner;

    public EventService(FieldActionsRunner actionsRunner) {
        this.actionsRunner = actionsRunner;
    }

    @Override
    public List<EventOutcome> runActions(List<Action> actions, Case useCase, Task task, Transition transition) {
        log.info("[" + useCase.getStringId() + "]: Running actions of transition " + transition.getStringId());
        return runActions(actions, useCase, Optional.of(task));
    }

    @Override
    public List<EventOutcome> runActions(List<Action> actions, Case useCase, Optional<Task> task) {
        List<EventOutcome> allOutcomes = new ArrayList<>();
        actions.forEach(action -> {
            List<EventOutcome> outcomes = actionsRunner.run(action, useCase, task);
            outcomes.stream().filter(SetDataEventOutcome.class::isInstance)
                    .forEach(outcome -> {
                        if (((SetDataEventOutcome) outcome).getChangedFields().isEmpty()) return;
                        runEventActionsOnChanged(useCase, task.get(), (SetDataEventOutcome) outcome, DataEventType.SET);
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
            if (transition.getDataSet().containsKey(field.getStringId()) && !transition.getDataSet().get(field.getStringId()).getEvents().isEmpty())
                fieldActions.addAll(DataFieldLogic.getEventAction(transition.getDataSet().get(field.getStringId()).getEvents().get(actionTrigger), phase));
        }

        if (fieldActions.isEmpty()) return Collections.emptyList();

        return runEventActions(useCase, task, fieldActions, actionTrigger);
    }

    @Override
    public List<EventOutcome> runEventActions(Case useCase, Task task, List<Action> actions, DataEventType trigger) {
        List<EventOutcome> allOutcomes = new ArrayList<>();
        actions.forEach(action -> {
            List<EventOutcome> outcomes = actionsRunner.run(action, useCase, task == null ? Optional.empty() : Optional.of(task));
            outcomes.stream().filter(SetDataEventOutcome.class::isInstance)
                    .forEach(outcome -> {
                        if (((SetDataEventOutcome) outcome).getChangedFields().isEmpty()) return;
                        runEventActionsOnChanged(useCase, task, (SetDataEventOutcome) outcome, trigger);
                    });
            allOutcomes.addAll(outcomes);
        });
        return allOutcomes;
    }

    @Override
    public void runEventActionsOnChanged(Case useCase, Task task, SetDataEventOutcome outcome, DataEventType trigger) {
        outcome.getChangedFields().forEach((s, changedField) -> {
            if ((changedField.getAttributes().containsKey("value") && changedField.getAttributes().get("value") != null) && trigger == DataEventType.SET) {
                Field field = useCase.getField(s);
                log.info("[" + useCase.getStringId() + "] " + useCase.getTitle() + ": Running actions on changed field " + s);
                outcome.addOutcomes(processDataEvents(field, trigger, EventPhase.PRE, useCase, task));
                outcome.addOutcomes(processDataEvents(field, trigger, EventPhase.POST, useCase, task));
            }
        });
    }

    @Override
    public ChangedFieldContainer mergeFieldsFromOutcomes(List<EventOutcome> outcomes, ChangedFieldContainer container, Task referencingTask) {
        outcomes.forEach(outcome -> {
            if (outcome instanceof SetDataEventOutcome && !((SetDataEventOutcome) outcome).getPropagatedChanges().isEmpty()) {
                parseOutcomes(((SetDataEventOutcome) outcome).getPropagatedChanges(), ((SetDataEventOutcome) outcome).getChangedFields(), referencingTask).forEach((fieldId, changedField) -> container.getChangedFields().put(fieldId, changedField.getAttributes()));
            }
            if (!outcome.getOutcomes().isEmpty())
                mergeFieldsFromOutcomes(outcome.getOutcomes(), container, referencingTask);
        });
        return container;
    }

    @Override
    public ChangedFieldContainer parseChangesFromOutcomes(List<EventOutcome> outcomes, ChangedFieldContainer container, Task referencedTask) {
        Map<String, CaseChangedFields> propagatedChanges = new HashMap<>();
        outcomes.forEach(outcome -> {
            if (outcome instanceof SetDataEventOutcome) {
                ((SetDataEventOutcome) outcome).getPropagatedChanges().forEach((id, caseChangedFields) -> {
                    if (!propagatedChanges.containsKey(id)) {
                        propagatedChanges.put(id, caseChangedFields);
                    } else {
                        propagatedChanges.get(id).mergeChanges(caseChangedFields.getChangedFields());
                    }
                });
                if (!propagatedChanges.containsKey(referencedTask.getCaseId())) {
                    propagatedChanges.put(referencedTask.getCaseId(), new CaseChangedFields(referencedTask.getCaseId(), ((SetDataEventOutcome) outcome).getChangedFields()));
                } else {
                    propagatedChanges.get(referencedTask.getCaseId()).mergeChanges(((SetDataEventOutcome) outcome).getChangedFields());
                }
            }
            propagatedChanges.values().forEach(caseChangedFields ->
                    caseChangedFields.getChangedFields().forEach((caseId, change) ->
                            change.getChangedOn().forEach(taskPair -> {
                                if (!taskPair.getTaskId().equals(referencedTask.getStringId())) {
                                    substituteTaskRefFieldBehavior(change, taskPair.getTransition(), taskPair.getTaskId(), referencedTask.getTransitionId());
                                }
                                if (caseChangedFields.getCaseId().equals(referencedTask.getCaseId())) {
                                    container.getChangedFields().put(caseId, change.getAttributes());
                                }
                                container.getChangedFields().put(taskPair.getTaskId() + "-" + caseId, change.getAttributes());
                            })
                    ));

            if (!outcome.getOutcomes().isEmpty()) {
                parseChangesFromOutcomes(outcome.getOutcomes(), container, referencedTask);
            }
        });

        return container;
    }


    private Map<String, ChangedField> parseOutcomes(Map<String, CaseChangedFields> propagatedChanges, Map<String, ChangedField> changedFields, Task referencingTask) {
        Map<String, ChangedField> result = new HashMap<>();
        propagatedChanges.forEach((id, caseFields) -> {
            Map<String, ChangedField> localChanges = new HashMap<>();
            caseFields.getChangedFields().forEach((fieldId, changedField) -> {
                changedField.getChangedOn().forEach(taskPair -> {
                    if (!taskPair.getTaskId().equals(referencingTask.getStringId())) {
                        substituteTaskRefFieldBehavior(changedField, taskPair.getTransition(), taskPair.getTaskId(), referencingTask.getTransitionId());
                    }
                    if (id.equals(referencingTask.getCaseId())) {
                        localChanges.put(id, changedField);
                    }
                    localChanges.put(taskPair.getTaskId() + "-" + id, changedField);
                });
            });
            merge(result, localChanges);
        });
        merge(result, changedFields);
        return result;
    }

    private void merge(Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedFields) {
        newChangedFields.forEach((fieldId, changedField) -> {
            if (changedFields.containsKey(fieldId)) {
                changedFields.get(fieldId).merge(changedField);
            } else {
                changedFields.put(fieldId, changedField);
            }
        });
    }

    private void substituteTaskRefFieldBehavior(ChangedField change, String referencedTaskTrans, String referencedTaskStringId, String refereeTransId) {
        substituteTaskRefFieldBehavior(change.getAttributes(), referencedTaskTrans, referencedTaskStringId, refereeTransId);
    }

    private void substituteTaskRefFieldBehavior(Map<String, Object> change, String referencedTaskTrans, String referencedTaskStringId, String refereeTransId) {
        if (change.containsKey("behavior")) {
            Map<String, Object> newBehavior = new HashMap<>();
            ((Map<String, Object>) change.get("behavior")).forEach((transId, behavior) -> {
                String behaviorChangedOnTrans = transId.equals(referencedTaskTrans) ?
                        refereeTransId : referencedTaskStringId + "-" + transId;
                newBehavior.put(behaviorChangedOnTrans, behavior);
            });
            change.put("behavior", newBehavior);
        }
    }

}
