package com.netgrif.application.engine.importer.service.evaluation;

import com.netgrif.application.engine.petrinet.domain.Function;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(prefix = "nae.importer", name = "evaluate-actions", havingValue = "true", matchIfMissing = true)
public class ActionEvaluator implements IActionEvaluator {

    private final ActionRunner actionRunner;

    public ActionEvaluator(ActionRunner actionRunner) {
        this.actionRunner = actionRunner;
    }

    @Override
    public void evaluate(List<Action> actions, List<Function> functions) {
        actions.forEach(action -> {
            try {
                actionRunner.getActionCode(action, functions, true);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not evaluate action[" + action.getImportId() + "]: \n " + action.getDefinition(), e);
            }
        });
    }
}
