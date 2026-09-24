package com.netgrif.application.engine.objects.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
