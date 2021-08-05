package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.Function;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.workflow.domain.CachedFunction;
import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

public interface IFieldActionsCacheService {

    void cachePetriNetFunctions(PetriNet petriNet);

    void removeCachePetriNetFunctions(PetriNet petriNet);

    Closure getCompiledAction(Action action);

    List<CachedFunction> getCachedFunctions(List<Function> functions);

    Map<String, List<CachedFunction>> getStaticFunctionCache();

    void clearActionCache();

    void clearStaticFunctionCache();

    void clearFunctionCache();
}
