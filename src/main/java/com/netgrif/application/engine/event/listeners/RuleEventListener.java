//package com.netgrif.application.engine.event.listeners;
//
//import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
//import com.netgrif.application.engine.event.dispatchers.common.DispatchMethod;
//import com.netgrif.application.engine.event.events.Event;
//import com.netgrif.application.engine.event.events.EventAction;
//import com.netgrif.application.engine.event.events.task.TaskEvent;
//import com.netgrif.application.engine.workflow.domain.Case;
//import com.netgrif.application.engine.workflow.domain.Task;
//import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
//import com.netgrif.netgrifdrools.rules.domain.facts.TransitionEventFact;
//import com.netgrif.netgrifdrools.rules.service.interfaces.IRuleEngine;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class RuleEventListener extends ContextEditingListener<TaskEvent> {
//    private final IWorkflowService workflowService;
//    private final IRuleEngine ruleEngine;
//
//    public RuleEventListener(AbstractDispatcher dispatcher, EventAction eventAction, DispatchMethod method,
//                             IWorkflowService workflowService, IRuleEngine ruleEngine) {
//        super(dispatcher, eventAction, method);
//        this.workflowService = workflowService;
//        this.ruleEngine = ruleEngine;
//    }
//
//    @Override
//    public TaskEvent onContextEditingEvent(Event event, AbstractDispatcher dispatcher) {
//        TaskEvent taskEvent = (TaskEvent) event;
//        Case useCase = taskEvent.getTaskEventOutcome().getCase();
//        Task task = taskEvent.getTaskEventOutcome().getTask();
//        log.info("[" + useCase.getStringId() + "]: Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] evaluating rules of event " +  taskEvent.getEventType().name() + " of phase " + taskEvent.getEventPhase().name());
//        int rulesExecuted = ruleEngine.evaluateRules(useCase, task, TransitionEventFact.of(task, taskEvent.getEventType(), taskEvent.getEventPhase()));
//        if (rulesExecuted == 0) {
//            taskEvent.getTaskEventOutcome().setCase(useCase);
//            taskEvent.getTaskEventOutcome().setTask(task);
//            return taskEvent;
//        }
//        taskEvent.getTaskEventOutcome().setCase(workflowService.save(useCase));
//        return taskEvent;
//    }
//
//    @Override
//    public void onEvent(Event event, AbstractDispatcher dispatcher) {
//
//    }
//
//    @Override
//    public void onAsyncEvent(Event event, AbstractDispatcher dispatcher) {
//
//    }
//}
