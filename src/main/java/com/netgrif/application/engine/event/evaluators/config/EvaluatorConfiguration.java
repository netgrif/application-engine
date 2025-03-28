package com.netgrif.application.engine.event.evaluators.config;

import com.netgrif.application.engine.event.evaluators.Evaluator;
import com.netgrif.core.event.events.workflow.CaseEvent;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvaluatorConfiguration {
    private IWorkflowService workflowService;
    private ApplicationEventPublisher publisher;

    @Autowired
    public void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Autowired
    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Bean
    public Evaluator<CaseEvent, Case> defaultCaseEvaluator() {
        return new Evaluator<>("default", event -> {
            publisher.publishEvent(event);
            return workflowService.findOne(event.getCaseEventOutcome().getCase().getStringId());
        });
    }

    @Bean
    public Evaluator<CaseEvent, Case> noContextCaseEvaluator() {
        return new Evaluator<>("noContext", event -> {
            publisher.publishEvent(event);
            return null;
        });
    }
}
