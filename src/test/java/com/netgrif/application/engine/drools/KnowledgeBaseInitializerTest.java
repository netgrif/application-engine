package com.netgrif.application.engine.drools;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.ApplicationEngine;
import com.netgrif.application.engine.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.application.engine.configuration.drools.throwable.RuleValidationException;
import com.netgrif.application.engine.rules.domain.StoredRule;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApplicationEngine.class
)
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class KnowledgeBaseInitializerTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IKnowledgeBaseInitializer knowledgeBaseInitializer;

    @BeforeEach
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

    @Test
    public void testInitializerRuleValidation_EXPECT_EXCEPTION() {
        assertThrows(RuleValidationException.class, () -> {
            StoredRule rule3 = StoredRule.builder()
                    ._id(new ObjectId())
                    .when("$item: Object()")
                    .then("log.info(' EXPECTING SYNTAX ERROR")
                    .identifier("rule3")
                    .lastUpdate(LocalDateTime.now())
                    .enabled(true)
                    .build();

            knowledgeBaseInitializer.validate(Collections.singletonList(rule3));
        });
    }

    @Test
    public void testInitializerRuleValidation_EXPECT_EXCEPTION2() throws RuleValidationException {
        assertThrows(RuleValidationException.class, () -> {
            StoredRule rule4 = StoredRule.builder()
                    ._id(new ObjectId())
                    .when("$item: Object")
                    .then("log.info('nothing')")
                    .identifier("rule4")
                    .lastUpdate(LocalDateTime.now())
                    .enabled(true)
                    .build();

            knowledgeBaseInitializer.validate(Collections.singletonList(rule4));
        });
    }


}
