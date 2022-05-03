package com.netgrif.application.engine.auth.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Authorize {

    AuthorityEnum authority() default AuthorityEnum.ADMIN;
    String preAuthorize() default "";
}
