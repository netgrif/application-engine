package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.TestHelper;
import com.netgrif.workflow.WorkflowManagementSystemApplication;
import com.netgrif.workflow.configuration.drools.RefreshableKieBase;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import com.netgrif.workflow.rules.service.interfaces.IRuleEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class RuleEngineTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IRuleEngine ruleEngine;
    
    @Autowired
    private RuleRepository ruleRepository;
    
    @Autowired
    private RefreshableKieBase refreshableKieBase;

    @Before
    public void before() {
        testHelper.truncateDbs();
    }
    
    @Test
    public void kieBaseTest() {
        refreshableKieBase.refresh();
        assert refreshableKieBase.kieBase() != null;
        assert !refreshableKieBase.shouldRefresh();

        StoredRule rule = StoredRule.builder()
                .when("$item: Object()")
                .then("log.info('nothing')")
                .identifier("random")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();
        ruleRepository.save(rule);

        assert refreshableKieBase.shouldRefresh();
        refreshableKieBase.refresh();
        assert !refreshableKieBase.shouldRefresh();
    }

    @Test
    public void testRule() {
        StoredRule rule = StoredRule.builder()
                .when("$case: PetriNet()   $scheduledEvent: ScheduledRuleFact()")
                .then("log.info($case.stringId + ' was evaluated in scheduled rule 1')  \n log.info('' + factRepository.findAll(QCaseCreatedFact.caseCreatedFact.caseId.eq($case.stringId)))")
                .identifier("random")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();



    }
    
    
}
