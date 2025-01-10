package com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ProcessEventOutcome extends EventOutcome {

    private Case templateCase;
    private Case processScopedCase;

    protected ProcessEventOutcome() {
    }

    protected ProcessEventOutcome(Case templateCase, Case processScopedCase) {
        this.templateCase = templateCase;
        this.processScopedCase = processScopedCase;
    }

    protected ProcessEventOutcome(I18nString message, Case templateCase, Case processScopedCase) {
        super(message);
        this.templateCase = templateCase;
        this.processScopedCase = processScopedCase;
    }

    protected ProcessEventOutcome(I18nString message, List<EventOutcome> outcomes, Case templateCase, Case processScopedCase) {
        super(message, outcomes);
        this.templateCase = templateCase;
        this.processScopedCase = processScopedCase;
    }
}

