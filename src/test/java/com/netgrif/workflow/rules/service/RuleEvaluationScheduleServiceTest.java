package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.TestHelper;
import com.netgrif.workflow.WorkflowManagementSystemApplication;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import com.netgrif.workflow.rules.service.interfaces.IRuleEvaluationScheduleService;
import com.netgrif.workflow.rules.service.throwable.RuleEvaluationScheduleException;
import com.netgrif.workflow.startup.SuperCreator;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class RuleEvaluationScheduleServiceTest {

    @Autowired
    private TestHelper testHelper;

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

    @Before
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void testScheduledRule() throws IOException, MissingPetriNetMetaDataException, RuleEvaluationScheduleException, InterruptedException {
        LoggedUser user = superCreator.getLoggedSuper();
        Optional<PetriNet> petriNetOptional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/rule_engine_test.xml"), "major", user);

        StoredRule rule = StoredRule.builder()
                .when("$case: Case() $event: ScheduledRuleFact(instanceId == $case.stringId, ruleIdentifier == \"rule2\")")
                .then("log.info(\"matched rule\"); \n $case.dataSet[\"number_data\"].value += " + 1.0 + "")
                .identifier("rule2")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();
        ruleRepository.save(rule);

        Case caze = workflowService.createCase(petriNetOptional.get().getStringId(), "Original title", "original color", user);
        ruleEvaluationScheduleService.scheduleRuleEvaluationForCase(caze, "rule2", SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1).withRepeatCount(5));

        Thread.sleep(10000);
        caze = workflowService.findOne(caze.getStringId());
        assert caze.getDataSet().get("number_data").getValue().equals(6.0);

    }



}
