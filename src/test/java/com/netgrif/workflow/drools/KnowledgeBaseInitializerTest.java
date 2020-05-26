package com.netgrif.workflow.drools;

import com.netgrif.workflow.TestHelper;
import com.netgrif.workflow.WorkflowManagementSystemApplication;
import com.netgrif.workflow.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.workflow.configuration.drools.throwable.RuleValidationException;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class KnowledgeBaseInitializerTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IKnowledgeBaseInitializer knowledgeBaseInitializer;

    @Autowired
    private RuleRepository ruleRepository;

    @Before
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void testInitializerRuleValidation() {
        StoredRule rule = StoredRule.builder()
                ._id(new ObjectId())
                .when("$item: Object()")
                .then("log.info('nothing')")
                .identifier("rule1")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();

        StoredRule rule2 = StoredRule.builder()
                ._id(new ObjectId())
                .when("$item: Object()")
                .then("log.info('nothing')")
                .identifier("rule2")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();

        knowledgeBaseInitializer.constructKieBase();

        try {
            knowledgeBaseInitializer.validate(Arrays.asList(rule, rule2));
        } catch (RuleValidationException e) {
            e.printStackTrace();
            throw new AssertionError(e);
        }
    }

    @Test(expected = RuleValidationException.class)
    public void testInitializerRuleValidation_EXPECT_EXCEPTION() throws RuleValidationException {

        StoredRule rule3 = StoredRule.builder()
                ._id(new ObjectId())
                .when("$item: Object()")
                .then("log.info(' EXPECTING SYNTAX ERROR")
                .identifier("rule3")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();

        knowledgeBaseInitializer.validate(Collections.singletonList(rule3));

    }
    @Test(expected = RuleValidationException.class)
    public void testInitializerRuleValidation_EXPECT_EXCEPTION2() throws RuleValidationException {

        StoredRule rule4 = StoredRule.builder()
                ._id(new ObjectId())
                .when("$item: Object")
                .then("log.info('nothing')")
                .identifier("rule4")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();

        knowledgeBaseInitializer.validate(Collections.singletonList(rule4));
    }


}
