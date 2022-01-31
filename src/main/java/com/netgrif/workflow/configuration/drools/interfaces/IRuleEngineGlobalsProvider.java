package com.netgrif.workflow.configuration.drools.interfaces;

import com.netgrif.workflow.configuration.drools.RuleEngineGlobal;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IRuleEngineGlobalsProvider {

    List<RuleEngineGlobal> globals();

    List<String> imports();

    void setGlobals(KieSession session);
}
