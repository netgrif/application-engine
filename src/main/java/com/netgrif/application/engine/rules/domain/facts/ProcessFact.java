package com.netgrif.application.engine.rules.domain.facts;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ProcessFact extends Fact {

    private String templateCaseId;

    public ProcessFact(String templateCaseId) {
        super();
        this.templateCaseId = templateCaseId;
    }
}
