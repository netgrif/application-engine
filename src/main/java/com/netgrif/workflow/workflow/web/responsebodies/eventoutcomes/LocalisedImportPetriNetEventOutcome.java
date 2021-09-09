package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;

import java.util.Locale;

public class LocalisedImportPetriNetEventOutcome extends LocalisedEventOutcome {

    public LocalisedImportPetriNetEventOutcome(ImportPetriNetEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
