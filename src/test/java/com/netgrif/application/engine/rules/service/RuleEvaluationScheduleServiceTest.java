package com.netgrif.application.engine.rules.service;

import com.netgrif.application.engine.EngineTest;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.rules.domain.RuleRepository;
import com.netgrif.application.engine.rules.domain.StoredRule;
import com.netgrif.application.engine.rules.domain.scheduled.ScheduleOutcome;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEvaluationScheduleService;
import com.netgrif.application.engine.rules.service.throwable.RuleEvaluationScheduleException;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class RuleEvaluationScheduleServiceTest {

    @Autowired
    private EngineTest testHelper;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private IRuleEvaluationScheduleService ruleEvaluationScheduleService;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testScheduledRule() throws IOException, MissingPetriNetMetaDataException, RuleEvaluationScheduleException, InterruptedException, MissingIconKeyException {
        LoggedUser user = superCreator.getLoggedSuper();
        ImportPetriNetEventOutcome importOutcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/rule_engine_test.xml"), VersionType.MAJOR, user);

        StoredRule rule = StoredRule.builder()
                .when("$case: Case() $event: ScheduledRuleFact(instanceId == $case.stringId, ruleIdentifier == \"rule2\")")
                .then("log.info(\"matched rule\"); \n $case.dataSet.get(\"number_data\").value.value += " + 1.0 + "; \n workflowService.save($case);")
                .identifier("rule2")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();
        ruleRepository.save(rule);

        CreateCaseEventOutcome caseOutcome = workflowService.createCase(importOutcome.getNet().getStringId(), "Original title", "original color", user);
        ScheduleOutcome outcome = ruleEvaluationScheduleService.scheduleRuleEvaluationForCase(caseOutcome.getCase(), "rule2", TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1).withRepeatCount(5)));

        assert outcome.getJobDetail() != null;
        assert outcome.getTrigger() != null;

        Thread.sleep(10000);
        String id = caseOutcome.getCase().getStringId();
        assert id != null;
        Case caze = workflowService.findOne(id);
        assert caze != null;
        assert caze.getDataSet().get("number_data").getValue().getValue().equals(5561.0);
    }

    @AfterEach
    public void after() {
        testHelper.truncateDbs();
    }

}
