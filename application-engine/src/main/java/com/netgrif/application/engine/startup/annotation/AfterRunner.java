package com.netgrif.application.engine.startup.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterRunner {

    Class<?> value();

}
