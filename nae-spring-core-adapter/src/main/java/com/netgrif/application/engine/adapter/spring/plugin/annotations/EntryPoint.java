package com.netgrif.application.engine.adapter.spring.plugin.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in NAE Plugin library to mark classes as they are Spring Beans (kind of
 * {@link Service}), that implement remotely callable plugin methods. These methods have
 * to be marked with {@link EntryPointMethod}.
 * */
@Service
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntryPoint {
    @AliasFor("name")
    String value() default "";
    @AliasFor("value")
    String name() default "";
    /**
     * This is only considered when plugin is loaded as module.
     * */
    String pluginName() default "";
}
