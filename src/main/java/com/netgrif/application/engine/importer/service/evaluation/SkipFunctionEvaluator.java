package com.netgrif.application.engine.importer.service.evaluation;

import com.netgrif.application.engine.workflow.domain.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "nae.importer", value = "evaluate-functions", havingValue = "false")
public class SkipFunctionEvaluator implements IFunctionEvaluator {
    @Override
    public void evaluate(List<Function> functions) {
        log.info("Skipping evaluation of functions");
    }
}
