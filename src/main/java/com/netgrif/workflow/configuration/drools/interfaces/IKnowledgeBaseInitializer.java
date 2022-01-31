package com.netgrif.workflow.configuration.drools.interfaces;

import com.netgrif.workflow.configuration.drools.throwable.RuleValidationException;
import com.netgrif.workflow.rules.domain.StoredRule;
import org.kie.api.KieBase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IKnowledgeBaseInitializer {

    KieBase constructKieBase();

    void validate(List<StoredRule> storedRule) throws RuleValidationException;
}
