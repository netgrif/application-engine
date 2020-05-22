package com.netgrif.workflow.startup

import com.netgrif.workflow.configuration.drools.RefreshableKieBase
import com.netgrif.workflow.configuration.drools.interfaces.IKnowledgeBaseInitializer
import com.netgrif.workflow.rules.domain.RuleRepository
import com.netgrif.workflow.rules.domain.StoredRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RuleEngineRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineRunner)

    @Autowired
    private RuleRepository repository

    @Autowired
    private RefreshableKieBase refreshableKieBase

    @Autowired
    private IKnowledgeBaseInitializer knowledgeBaseInitializer

    @Override
    void run(String... strings) throws Exception {
        refreshableKieBase.refresh()

        StoredRule rule = new StoredRule()
        rule.when = "\$case: Case()"
        rule.then = "log.info(\$case.stringId + ' was evaluated in rule 1')    " +
                "\n log.info(\"\" + factRepository.findAll(QCaseCreatedFact.caseCreatedFact.caseId.eq(\$case.stringId)))"

        repository.save(rule)

        StoredRule rule2 = new StoredRule()
        rule2.when = "\$case: Case()\n    \$event: CaseCreatedFact(caseId == \$case.stringId)"
        rule2.then = "log.info(\$case.stringId + ' was created, rule 2 ' + \$event.eventPhase)     \n factRepository.save(\$event)     \n \$case.title = 'NEW TITLE ' + \$event.eventPhase"
        repository.save(rule2)

        StoredRule rule3 = new StoredRule()
        rule3.when = "\$case: Case()\n    \$event: TransitionEventFact(type == EventType.FINISH, phase == EventPhase.POST)"
        rule3.then = "log.info(\$case.stringId + ' was evaluated in rule 3, finished task (POST) ' + \$event.transitionId) \n factRepository.save(\$event)"
        repository.save(rule3)

        StoredRule rule4 = new StoredRule()
        rule4.when = "\$case: Case()\n    \$event: TransitionEventFact(type == EventType.FINISH, phase == EventPhase.PRE)"
        rule4.then = "log.info(\$case.stringId + ' was evaluated in rule 4, finished task (PRE) ' + \$event.transitionId) \n factRepository.save(\$event)"
        repository.save(rule4)

        StoredRule rule5 = new StoredRule()
        rule5.when = "\$net: PetriNet()\n    \$event: NetImportedFact(netId == \$net.stringId, eventPhase == EventPhase.PRE)\n    \$facts: ArrayList() from collect (Fact() from factRepository.findAll())\n"
        rule5.then = "log.info(\$net.stringId + ' ' + \$net.identifier + ' was imported  ' + \$event.eventPhase)     \n factRepository.save(\$event)     \n log.info(\"Count = \" + \$facts)    \n \$net.title.defaultValue = 'NEW TITLE ' + \$event.eventPhase"
        repository.save(rule5)

    }

}