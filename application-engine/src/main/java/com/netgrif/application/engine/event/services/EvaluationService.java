package com.netgrif.application.engine.event.services;

import com.netgrif.application.engine.event.evaluators.Evaluator;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public interface EvaluationService {
    Map<String, Evaluator<?, ?>> getRegistry();
    Evaluator<?, ?> getEvaluator(String key);
}
