package com.netgrif.application.engine.eventoutcomes

import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome

class LocalisedEventOutcomeFactory {

    private static final String localisedEventOutcomesPackage = "com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes."

    static LocalisedEventOutcome from(EventOutcome outcome, Locale locale){
//        todo test https://github.com/ronmamo/reflections
        return Class.forName(localisedEventOutcomesPackage + "Localised${outcome.class.simpleName}").newInstance(outcome, locale)
    }
}
