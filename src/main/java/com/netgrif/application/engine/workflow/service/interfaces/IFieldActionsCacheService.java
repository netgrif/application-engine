package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Function;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

public interface IFieldActionsCacheService {

    void cachePetriNetFunctions(Process petriNet);

    void reloadCachedFunctions(Process petriNet);

    Closure getCompiledAction(Action action, boolean shouldRewriteCachedActions);

    List<CachedFunction> getCachedFunctions(List<Function> functions);

    Map<String, List<CachedFunction>> getNamespaceFunctionCache();

    void evaluateFunctions(List<Function> functions);

    void clearActionCache();

    void clearNamespaceFunctionCache();

    void clearFunctionCache();
}
