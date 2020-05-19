package com.netgrif.workflow.rules.domain.facts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseEvaluation extends Fact {

    private String caseId;

    private LocalDateTime evaluationTime;

}


