package com.netgrif.application.engine.objects.eventoutcomes;
//
//import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.EventOutcome;
//import com.netgrif.application.engine.objects.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
//
//import java.lang.reflect.InvocationTargetException;
//import java.util.Locale;
//
//public class LocalisedEventOutcomeFactory {
//    private static final String localisedEventOutcomesPackage = "com.netgrif.application.engine.objects.workflow.web.responsebodies.eventoutcomes.";
//
//
//    public static <T extends EventOutcome> LocalisedEventOutcome from(T outcome, Locale locale) {
//        try {
//            return ((LocalisedEventOutcome) (Class.forName(localisedEventOutcomesPackage + "Localised" + outcome.getClass().getSimpleName()).getDeclaredConstructor(outcome.getClass(), Locale.class).newInstance(outcome, locale)));
//        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
