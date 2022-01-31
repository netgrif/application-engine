package com.netgrif.application.engine.configuration.drools;

import com.netgrif.application.engine.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.application.engine.configuration.drools.interfaces.IRefreshableKieBase;
import com.netgrif.application.engine.rules.domain.RuleRepository;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RefreshableKieBase implements IRefreshableKieBase {

    public static final Logger log = LoggerFactory.getLogger(RefreshableKieBase.class);

    @Value("${drools.knowBase.auto-refresh:#{true}}")
    private boolean autoRefresh;

    private LocalDateTime lastRefresh;
    private KieBase kieBase;

    private final RuleRepository ruleRepository;
    private final IKnowledgeBaseInitializer knowledgeBaseInitializer;


    public RefreshableKieBase(@Autowired RuleRepository ruleRepository, @Autowired IKnowledgeBaseInitializer knowledgeBaseInitializer) {
        this.ruleRepository = ruleRepository;
        this.knowledgeBaseInitializer = knowledgeBaseInitializer;
    }

    public KieBase kieBase() {
        return kieBase;
    }

    public boolean shouldRefresh() {
        return autoRefresh && ruleRepository.existsByLastUpdateAfter(lastRefresh);
    }

    public void refresh() {
        this.lastRefresh = LocalDateTime.now();
        log.info("Refreshing kieBase");
        this.kieBase = knowledgeBaseInitializer.constructKieBase();
        log.info("KieBase refreshed");
    }
}
