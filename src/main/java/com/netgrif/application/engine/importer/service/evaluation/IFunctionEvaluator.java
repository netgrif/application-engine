package com.netgrif.application.engine.importer.service.evaluation;

import com.netgrif.application.engine.petrinet.domain.Function;

import java.util.List;

public interface IFunctionEvaluator {

    void evaluate(List<Function> functions);
}