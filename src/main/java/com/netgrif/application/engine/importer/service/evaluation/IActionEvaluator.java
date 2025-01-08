package com.netgrif.application.engine.importer.service.evaluation;

import com.netgrif.application.engine.workflow.domain.Function;
import com.netgrif.application.engine.workflow.domain.dataset.logic.action.Action;

import java.util.List;

public interface IActionEvaluator {

    void evaluate(List<Action> actions, List<Function> functions);
}
