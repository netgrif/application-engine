package com.netgrif.application.engine.auth.domain;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
public @interface Authorizations {

    Authorize[] value();
}
