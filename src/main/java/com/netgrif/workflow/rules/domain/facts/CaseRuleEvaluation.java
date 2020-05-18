package com.netgrif.workflow.rules.domain.facts;

import com.netgrif.workflow.rules.domain.facts.RuleFact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseRuleEvaluation extends RuleFact {

    private String caseId;

    private LocalDateTime evaluationTime;

}


