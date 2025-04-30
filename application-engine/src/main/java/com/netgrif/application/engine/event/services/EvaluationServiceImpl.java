package com.netgrif.application.engine.event.services;

import com.netgrif.application.engine.event.evaluators.Evaluator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Slf4j
@Service
public class EvaluationServiceImpl implements EvaluationService {
    private final Map<String, Evaluator<?, ?>> evaluationFunctionRegistry;

    public EvaluationServiceImpl(Set<Evaluator<?, ?>> evaluatorSet) {
        this.evaluationFunctionRegistry = evaluatorSet.stream().collect(Collectors.toMap(Evaluator::getType, Function.identity()));
    }

    @Override
    public Map<String, Evaluator<?, ?>> getRegistry() {
        return evaluationFunctionRegistry;
    }

    @Override
    public Evaluator<?, ?> getEvaluator(String key) {
        return evaluationFunctionRegistry.get(key);
    }
}
