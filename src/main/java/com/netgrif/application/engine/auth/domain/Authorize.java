package com.netgrif.application.engine.auth.domain;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
public @interface Authorize {

    AuthorityEnum authority() default AuthorityEnum.ADMIN;

    @Language("SpEL")
    String expression() default "";
}
