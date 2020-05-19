package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.facts.TransitionEvent;
import com.netgrif.workflow.rules.service.interfaces.IRuleEngine;
import com.netgrif.workflow.workflow.domain.Case;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public abstract class RuleEngine implements IRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    @Lookup
    protected abstract KieSession ruleEngine();

    @Override
    public void evaluateRules(Case useCase) {
        KieSession ruleEngine = createSession();
        evaluateWithFacts(ruleEngine, Collections.singletonList(useCase));
    }

    @Override
    public void evaluateRules(Case useCase, TransitionEvent transitionEvent) {
        KieSession ruleEngine = createSession();
        evaluateWithFacts(ruleEngine, Arrays.asList(useCase, transitionEvent));
    }

    @Override
    public void evaluateRules(PetriNet petriNet) {
        KieSession ruleEngine = createSession();
        evaluateWithFacts(ruleEngine, Collections.singletonList(petriNet));
    }

    protected KieSession createSession() {
        return ruleEngine();
    }

    private void evaluateWithFacts(KieSession session, List<Object> facts) {
        facts.forEach(session::insert);
        session.fireAllRules();
        session.destroy();
    }
}
