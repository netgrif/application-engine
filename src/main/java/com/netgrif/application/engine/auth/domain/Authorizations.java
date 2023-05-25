package com.netgrif.application.engine.auth.domain;

import java.lang.annotation.*;

/**
 * Annotation to define set of authorizing statements
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Authorizations {

    /**
     * The array of authorizing statements, access will be granted, if one of these is true.
     * */
    Authorize[] value();
}
