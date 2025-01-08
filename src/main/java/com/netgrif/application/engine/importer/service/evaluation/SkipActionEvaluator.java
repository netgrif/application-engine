package com.netgrif.application.engine.importer.service.evaluation;

import com.netgrif.application.engine.workflow.domain.Function;
import com.netgrif.application.engine.workflow.domain.dataset.logic.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "nae.importer", name = "evaluate-actions", havingValue = "false")
public class SkipActionEvaluator implements IActionEvaluator {

    @Override
    public void evaluate(List<Action> actions, List<Function> functions) {
        log.info("Skipping evaluation of actions");
    }
}
