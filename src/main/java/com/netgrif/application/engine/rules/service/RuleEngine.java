package com.netgrif.application.engine.rules.service;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.domain.facts.NetImportedFact;
import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.application.engine.rules.domain.facts.TransitionEventFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public abstract class RuleEngine implements IRuleEngine {

    // TODO: release/7.0.0 properties
    @Value("${rule-engine.rethrow-exceptions:#{false}}")
    protected boolean rethrowExceptions;

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
        KieSession session = null;
        try {
            session = createSession();
            facts.forEach(session::insert);
            session.fireAllRules();
        } catch (Exception e) {
            log.error("Rule engine failure", e);
            if (rethrowExceptions) {
                throw e;
            }
        } finally {
            if (session != null) {
                session.destroy();
            }
        }
    }

    protected KieSession createSession() {
        return ruleEngine();
    }
}
