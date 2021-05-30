package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner.CaseFieldsExpressionRunner;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.ICaseInitValueExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CaseInitValueExpressionEvaluator extends AbstractInitValueExpressionEvaluator<Case> implements ICaseInitValueExpressionEvaluator {

    @Autowired
    private CaseFieldsExpressionRunner runner;

    @Override
    public <T> T evaluate(Case useCase, Field<T> defaultField) {
        return (T) evaluate(useCase, defaultField.getInitExpression());
    }

    @Override
    public I18nString evaluateCaseName(Case useCase, Expression expression) {
        Object result = evaluate(useCase, expression);
        if (result instanceof I18nString) {
            return (I18nString) result;
        } else {
            return new I18nString(result.toString());
        }
    }

    @Override
    String getStringId(Case useCase) {
        return useCase.getStringId();
    }

    @Override
    public Object evaluate(Case useCase, Expression expression) {
        return runner.run(useCase, expression);
    }
}
