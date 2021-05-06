package com.netgrif.workflow.drools;

import com.netgrif.workflow.TestHelper;
import com.netgrif.workflow.WorkflowManagementSystemApplication;
import com.netgrif.workflow.configuration.drools.RefreshableKieBase;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class RefreshableKieBaseTest {

    public static final Logger log = LoggerFactory.getLogger(RefreshableKieBaseTest.class);

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private RefreshableKieBase refreshableKieBase;

    @Autowired
    private RuleRepository ruleRepository;

    @Before
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void kieBaseTest() {
        refreshableKieBase.refresh();
        assert refreshableKieBase.kieBase() != null;
        assert !refreshableKieBase.shouldRefresh();

        StoredRule rule = basicRule();
        ruleRepository.save(rule);

        assert refreshableKieBase.shouldRefresh();
        refreshableKieBase.refresh();
        assert !refreshableKieBase.shouldRefresh();
    }


    @Test
    public void kieBaseSessionTest() {
        StoredRule rule = basicRule();
        ruleRepository.save(rule);
        refreshableKieBase.refresh();

        KieSession session1 = refreshableKieBase.kieBase().newKieSession();
        KieSession session2 = refreshableKieBase.kieBase().newKieSession();
        session1.setGlobal("log", log);
        session2.setGlobal("log", log);

        session1.insert(new AtomicInteger(1));
        FactHandle h1 = session2.insert(new AtomicInteger(2));
        FactHandle h2 = session2.insert(new AtomicInteger(3));

        refreshableKieBase.refresh();

        int amount1 = session1.fireAllRules();
        int amount2 = session2.fireAllRules();

        assert amount1 == 1;
        assert amount2 == 2;

        session1.destroy();

        rule.setLastUpdate(LocalDateTime.now());
        ruleRepository.save(rule);

        assert refreshableKieBase.shouldRefresh();
        refreshableKieBase.refresh();

        session2.delete(h1);
        session2.delete(h2);

        assert session2.getFactHandles().size() == 0;

        session2.insert(new AtomicInteger(4));

        amount2 = session2.fireAllRules();
        assert amount2 == 1;

        session2.destroy();
    }

    @Test
    public void ruleDisabledTest() {
        StoredRule rule = basicRule();
        ruleRepository.save(rule);

        refreshableKieBase.refresh();
        KieSession session = refreshableKieBase.kieBase().newKieSession();
        session.setGlobal("log", log);

        session.insert(new AtomicInteger(1));
        int amount = session.fireAllRules();
        assert amount == 1;
        session.destroy();

        rule.setEnabled(false);
        ruleRepository.save(rule);

        session = newSession();

        session.insert(new AtomicInteger(2));
        amount = session.fireAllRules();
        assert amount == 0;
        session.destroy();

        rule.setEnabled(true);
        rule.setDateEffective(LocalDate.now().minusDays(1));
        rule.setDateExpires(LocalDate.now().plusDays(2));
        ruleRepository.save(rule);

        session = newSession();

        session.insert(new AtomicInteger(3));
        amount = session.fireAllRules();
        assert amount == 1;
        session.destroy();

        rule.setDateEffective(LocalDate.now().minusDays(2));
        rule.setDateExpires(LocalDate.now().minusDays(1));
        ruleRepository.save(rule);

        session = newSession();

        session.insert(new AtomicInteger(4));
        amount = session.fireAllRules();
        assert amount == 0;
        session.destroy();

    }

    private StoredRule basicRule() {
        return StoredRule.builder()
                .when("$item: Object()")
                .then("log.info(\"ITEM: \" + $item.toString())")
                .identifier("random")
                .lastUpdate(LocalDateTime.now())
                .enabled(true)
                .build();
    }

    private KieSession newSession() {
        refreshableKieBase.refresh();
        KieSession session = refreshableKieBase.kieBase().newKieSession();
        session.setGlobal("log", log);
        return session;
    }

}
