package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.rules.domain.RuleFactRepository;
import com.netgrif.workflow.workflow.domain.Case;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CaseSessionService {

    private static final Logger log = LoggerFactory.getLogger(CaseSessionService.class);

    @Autowired
    private RuleEngineSessionService ruleEngineSessionService;

    @Autowired
    private RuleFactRepository ruleFactRepository;

    public KieSession getSessionForCase(Case useCase) {
        KieSession session = ruleEngineSessionService.createNewSession();
        return session;
    }
    
}
