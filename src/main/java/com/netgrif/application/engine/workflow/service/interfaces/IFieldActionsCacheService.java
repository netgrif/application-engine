package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.core.petrinet.domain.Function;
import com.netgrif.adapter.petrinet.domain.PetriNet;
import com.netgrif.core.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

public interface IFieldActionsCacheService {

    void cachePetriNetFunctions(PetriNet petriNet);

    void reloadCachedFunctions(PetriNet petriNet);

    Closure getCompiledAction(Action action, boolean shouldRewriteCachedActions);

    List<CachedFunction> getCachedFunctions(List<Function> functions);

    Map<String, List<CachedFunction>> getNamespaceFunctionCache();

    void evaluateFunctions(List<Function> functions);

    void clearActionCache();

    void clearNamespaceFunctionCache();

    void clearFunctionCache();
}
