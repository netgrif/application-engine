package com.netgrif.application.engine.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface NaeMixin {
}
