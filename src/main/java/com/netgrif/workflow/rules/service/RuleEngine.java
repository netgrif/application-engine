package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.facts.CaseCreatedFact;
import com.netgrif.workflow.rules.domain.facts.NetImportedFact;
import com.netgrif.workflow.rules.domain.facts.TransitionEventFact;
import com.netgrif.workflow.rules.service.interfaces.IRuleEngine;
import com.netgrif.workflow.workflow.domain.Case;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public abstract class RuleEngine implements IRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    @Lookup
    protected abstract KieSession ruleEngine();

    @Override
    public void evaluateRules(List<Object> facts) {
        KieSession ruleEngine = createSession();
        evaluateWithFacts(ruleEngine, facts);
    }

    @Override
    public void evaluateRules(Case useCase, CaseCreatedFact caseCreatedFact) {
        KieSession ruleEngine = createSession();
        evaluateWithFacts(ruleEngine, Arrays.asList(useCase, caseCreatedFact));
    }

    @Override
    public void evaluateRules(Case useCase, TransitionEventFact transitionEventFact) {
        KieSession ruleEngine = createSession();
        evaluateWithFacts(ruleEngine, Arrays.asList(useCase, transitionEventFact));
    }

    @Override
    public void evaluateRules(PetriNet petriNet, NetImportedFact fact) {
        KieSession ruleEngine = createSession();
        evaluateWithFacts(ruleEngine, Arrays.asList(petriNet, fact));
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
