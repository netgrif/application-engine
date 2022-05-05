package com.netgrif.application.engine.auth.domain;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;

/**
 * This annotation serves as marking for functions that requires additional authorization over the basic authentication.
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
public @interface Authorize {

    /**
     * The authorizing object to be checked, whether the user has it assigned.
     * */
    AuthorizingObject authority() default AuthorizingObject.ADMIN;

    /**
     * The Spring-EL expression to be evaluated before invoking the protected method.
     * */
    @Language("SpEL")
    String expression() default "";
}
