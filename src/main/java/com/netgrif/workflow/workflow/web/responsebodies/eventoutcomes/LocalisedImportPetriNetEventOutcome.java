package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedPetriNetEventOutcome;

import java.util.Locale;

public class LocalisedImportPetriNetEventOutcome extends LocalisedPetriNetEventOutcome {

    public LocalisedImportPetriNetEventOutcome(ImportPetriNetEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
