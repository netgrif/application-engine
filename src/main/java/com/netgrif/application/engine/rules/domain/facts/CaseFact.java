package com.netgrif.application.engine.rules.domain.facts;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public abstract class CaseFact extends Fact {

    private String caseId;

    public CaseFact(String caseId) {
        super();
        this.caseId = caseId;
    }
}
