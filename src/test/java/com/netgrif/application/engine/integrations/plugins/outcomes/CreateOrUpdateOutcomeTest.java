package com.netgrif.application.engine.integrations.plugins.outcomes;

import com.netgrif.pluginlibrary.core.outcomes.CreateOrUpdateOutcome;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test"})
public class CreateOrUpdateOutcomeTest {

    @Test
    public void testAddCreatedAndSubjectCaseId() {
        CreateOrUpdateOutcome outcome = new CreateOrUpdateOutcome();

        outcome.addCreatedAndSubjectCaseId("test-case-id");

        assert outcome.getCreatedCaseIds().contains("test-case-id") : "Collection createdCaseIds has missing id";
        assert outcome.getSubjectCaseIds().contains("test-case-id") : "Collection subjectCaseIds has missing id";
    }
}
