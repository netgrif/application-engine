package com.netgrif.application.engine.auth.domain;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;

/**
 * This annotation serves as marking for functions that requires additional authorization over the basic authentication.
 * */
@Repeatable(Authorizations.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Authorize {

    /**
     * The authorizing object to be checked, whether the user has it assigned.
     * */
    String[] authority() default "";

    /**
     * The Spring-EL expression to be evaluated before invoking the protected method.
     * */
    @Language("SpEL")
    String expression() default "";
}
