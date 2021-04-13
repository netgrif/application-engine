package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.petrinet.domain.dataset.FieldWithDefault;
import com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions.InitDataExpressions;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IInitValueExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InitValueExpressionEvaluator implements IInitValueExpressionEvaluator {

    @Autowired
    private InitDataExpressions initDataExpressions;

    @Override
    public Object evaluate(Case useCase, FieldWithDefault defaultField) {
        return initDataExpressions.compile(useCase, defaultField.getExpression());
    }
}
