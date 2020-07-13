package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.facts.CaseCreatedFact;
import com.netgrif.workflow.rules.domain.facts.NetImportedFact;
import com.netgrif.workflow.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.workflow.rules.domain.facts.TransitionEventFact;
import com.netgrif.workflow.rules.service.interfaces.IRuleEngine;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
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
    public void evaluateRules(Case useCase, CaseCreatedFact caseCreatedFact) {
        evaluateWithFacts(Arrays.asList(useCase, caseCreatedFact));
    }

    @Override
    public void evaluateRules(Case useCase, Task task, TransitionEventFact transitionEventFact) {
        evaluateWithFacts(Arrays.asList(useCase, task, transitionEventFact));
    }

    @Override
    public void evaluateRules(Case useCase, ScheduledRuleFact scheduledRuleFact) {
        evaluateWithFacts(Arrays.asList(useCase, scheduledRuleFact));
    }

    @Override
    public void evaluateRules(PetriNet petriNet, NetImportedFact fact) {
        evaluateWithFacts(Arrays.asList(petriNet, fact));
    }

    @Override
    public void evaluateRules(PetriNet petriNet, ScheduledRuleFact scheduledRuleFact) {
        evaluateWithFacts(Arrays.asList(petriNet, scheduledRuleFact));
    }


    private void evaluateWithFacts(List<Object> facts) {
        KieSession session = createSession();
        facts.forEach(session::insert);
        try {
            session.fireAllRules();
        } catch (Exception e) {
            log.error("Rule engine failure", e);
            throw e;
        } finally {
            session.destroy();
        }
    }

    protected KieSession createSession() {
        return ruleEngine();
    }
}
