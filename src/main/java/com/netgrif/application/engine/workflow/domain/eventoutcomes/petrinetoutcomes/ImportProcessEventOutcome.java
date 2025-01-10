package com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class ImportProcessEventOutcome extends ProcessEventOutcome {

    public ImportProcessEventOutcome() {
    }

    public ImportProcessEventOutcome(Case templateCase, Case processScopedCase) {
        super(templateCase, processScopedCase);
    }

    public ImportProcessEventOutcome(I18nString message, Case templateCase, Case processScopedCase) {
        super(message, templateCase, processScopedCase);
    }

    public ImportProcessEventOutcome(I18nString message, List<EventOutcome> outcomes, Case templateCase, Case processScopedCase) {
        super(message, outcomes, templateCase, processScopedCase);
    }
}
