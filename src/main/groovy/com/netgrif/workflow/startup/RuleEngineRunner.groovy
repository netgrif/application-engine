package com.netgrif.workflow.startup

import com.netgrif.workflow.rules.domain.StoredRule
import com.netgrif.workflow.rules.domain.RuleRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RuleEngineRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineRunner)

    @Autowired
    private RuleRepository repository;

    @Override
    void run(String... strings) throws Exception {
        StoredRule rule = new StoredRule()
        rule.when = "\$case: Case()"
        rule.then = "log.info(\$case.stringId + ' was evaluated in rule 1')"
        repository.save(rule)

        StoredRule rule2 = new StoredRule()
        rule2.when = "\$case: Case()"
        rule2.then = "log.info(\$case.stringId + ' was evaluated in rule 2')"
        repository.save(rule2)

        StoredRule rule3 = new StoredRule()
        rule3.when = "\$case: Case()\n    \$event: TransitionEvent(type == EventType.FINISH)"
        rule3.then = "log.info(\$case.stringId + ' was evaluated in rule 3, finished task ' + \$event.transitionId)"
        repository.save(rule3)
    }

}