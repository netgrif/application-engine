package com.netgrif.application.engine.rules.service;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.rules.config.RuleProperties;
import com.netgrif.application.engine.rules.domain.RuleRepository;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.domain.facts.NetImportedFact;
import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.application.engine.rules.domain.facts.TransitionEventFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class RuleEngine implements IRuleEngine {

    private final RuleRepository ruleRepository;

    private final RuleProperties properties;

    @Lookup
    protected abstract KieSession ruleEngine();

    @Override
    public int evaluateRules(Case useCase, CaseCreatedFact caseCreatedFact) {
        return evaluateWithFacts(Arrays.asList(useCase, caseCreatedFact));
    }

    @Override
    public int evaluateRules(Case useCase, Task task, TransitionEventFact transitionEventFact) {
        return evaluateWithFacts(Arrays.asList(useCase, task, transitionEventFact));
    }

    @Override
    public int evaluateRules(Case useCase, ScheduledRuleFact scheduledRuleFact) {
        return evaluateWithFacts(Arrays.asList(useCase, scheduledRuleFact));
    }

    @Override
    public int evaluateRules(PetriNet petriNet, NetImportedFact fact) {
        return evaluateWithFacts(Arrays.asList(petriNet, fact));
    }

    @Override
    public int evaluateRules(PetriNet petriNet, ScheduledRuleFact scheduledRuleFact) {
        return evaluateWithFacts(Arrays.asList(petriNet, scheduledRuleFact));
    }

    private int evaluateWithFacts(List<Object> facts) {
        if (!properties.isEnabled() || ruleRepository.count() == 0) {
            return 0;
        }
        KieSession session = null;
        int numberOfRulesExecuted = 0;
        try {
            session = createSession();
            facts.forEach(session::insert);
            numberOfRulesExecuted = session.fireAllRules();
        } catch (Exception e) {
            log.error("Rule engine failure", e);
            if (properties.isRethrowExceptions()) {
                throw e;
            }
        } finally {
            if (session != null) {
                session.destroy();
            }
        }
        return numberOfRulesExecuted;
    }

    protected KieSession createSession() {
        return ruleEngine();
    }
}
