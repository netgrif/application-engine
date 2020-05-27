package com.netgrif.workflow.configuration.drools;

import com.netgrif.workflow.configuration.drools.interfaces.IKnowledgeBaseInitializer;
import com.netgrif.workflow.configuration.drools.interfaces.IRefreshableKieBase;
import com.netgrif.workflow.rules.domain.RuleRepository;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RefreshableKieBase implements IRefreshableKieBase {

    private LocalDateTime lastRefresh;
    private KieBase kieBase;

    private RuleRepository ruleRepository;
    private IKnowledgeBaseInitializer knowledgeBaseInitializer;


    public RefreshableKieBase(@Autowired RuleRepository ruleRepository, @Autowired IKnowledgeBaseInitializer knowledgeBaseInitializer) {
        this.ruleRepository = ruleRepository;
        this.knowledgeBaseInitializer = knowledgeBaseInitializer;
    }

    public KieBase kieBase() {
        return kieBase;
    }

    public boolean shouldRefresh() {
        return ruleRepository.existsByLastUpdateAfter(lastRefresh);
    }

    public void refresh() {
        this.lastRefresh = LocalDateTime.now();
        this.kieBase = knowledgeBaseInitializer.constructKieBase();
    }
}
