package com.netgrif.application.engine.adapter.spring.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in NAE Plugin library to mark methods, that can be called from NAE application,
 * that the given plugin is attached to. These methods must be implemented as member of class, that is marked
 * with {@link EntryPoint}.
 * */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntryPointMethod {
    String name() default "";
    String description() default "";
    ListenerFilter[] listeners() default {};
}
