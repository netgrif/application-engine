package com.netgrif.application.engine.integration.modules;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleService {

    @AliasFor("module")
    String value() default "";

    @AliasFor("value")
    String module() default "";

}
