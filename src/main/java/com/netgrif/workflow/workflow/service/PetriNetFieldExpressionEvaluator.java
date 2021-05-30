package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner.PetriNetFieldsExpressionRunner;
import com.netgrif.workflow.workflow.service.interfaces.IPetriNetFieldExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PetriNetFieldExpressionEvaluator extends AbstractFieldExpressionEvaluator<PetriNet> implements IPetriNetFieldExpressionEvaluator {

    @Autowired
    protected PetriNetFieldsExpressionRunner runner;

    @Override
    String getStringId(PetriNet petriNet) {
        return petriNet.getStringId();
    }

    @Override
    public Object evaluate(PetriNet petriNet, Expression expression) {
        return runner.run(petriNet, expression);
    }
}
