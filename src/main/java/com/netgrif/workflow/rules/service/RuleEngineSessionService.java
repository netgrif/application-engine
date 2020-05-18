package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.rules.service.interfaces.IRuleEngineSessionService;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

@Service
public abstract class RuleEngineSessionService implements IRuleEngineSessionService {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineSessionService.class);

    @Lookup
    protected abstract KieSession ruleEngine();

    public KieSession createNewSession() {
        return ruleEngine();
    }

}
