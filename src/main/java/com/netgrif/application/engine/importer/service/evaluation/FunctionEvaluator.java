package com.netgrif.application.engine.importer.service.evaluation;

import com.netgrif.application.engine.petrinet.domain.Function;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "nae.importer", value = "evaluate-functions", havingValue = "true", matchIfMissing = true)
public class FunctionEvaluator implements IFunctionEvaluator {

    protected final IFieldActionsCacheService actionsCacheService;

    public FunctionEvaluator(IFieldActionsCacheService actionsCacheService) {
        this.actionsCacheService = actionsCacheService;
    }

    @Override
    public void evaluate(List<Function> functions) {
        try {
            actionsCacheService.evaluateFunctions(functions);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not evaluate functions: " + e.getMessage(), e);
        }
    }
}
