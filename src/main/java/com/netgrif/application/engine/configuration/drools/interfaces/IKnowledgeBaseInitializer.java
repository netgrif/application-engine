package com.netgrif.application.engine.configuration.drools.interfaces;

import com.netgrif.application.engine.configuration.drools.throwable.RuleValidationException;
import com.netgrif.application.engine.rules.domain.StoredRule;
import org.kie.api.KieBase;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IKnowledgeBaseInitializer {

    KieBase constructKieBase();

    void validate(List<StoredRule> storedRule) throws RuleValidationException;
}
