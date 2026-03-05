package com.netgrif.application.engine.adapter.spring.event;


@FunctionalInterface
public interface EventEnricher {

    <T> T enrich(T event);

}
