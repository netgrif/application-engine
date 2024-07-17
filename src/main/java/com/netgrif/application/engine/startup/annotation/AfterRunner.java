package com.netgrif.application.engine.startup.annotation;

import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterRunner {

    Class<? extends AbstractOrderedApplicationRunner> value();

}
