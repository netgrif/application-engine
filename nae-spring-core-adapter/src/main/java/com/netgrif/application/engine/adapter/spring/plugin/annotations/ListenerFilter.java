package com.netgrif.application.engine.adapter.spring.plugin.annotations;

import com.netgrif.application.engine.objects.event.dispatchers.common.AbstractDispatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EventObject;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListenerFilter {
    Class<? extends EventObject> eventType();
    AbstractDispatcher.DispatchMethod dispatchMethod();
}
