package com.netgrif.application.engine.startup.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Repeatable(RunnerOrders.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunnerOrder {

    int value() default Integer.MAX_VALUE;

}