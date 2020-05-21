package com.netgrif.workflow.rules.service.interfaces;

import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IRuleEvaluationScheduleService {

    void scheduleRuleEvaluationForCase(Case useCase, String ruleIdentifier);

    void scheduleRuleEvaluationForCase(Case useCase, List<String> ruleIdentifiers);

}
