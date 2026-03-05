package com.netgrif.application.engine.adapter.spring.event;


import org.springframework.context.ApplicationEventPublisher;

public interface NetgrifEventPublisher extends ApplicationEventPublisher {

    void addBeforePublish(EventEnricher eventEnricher);

    void removeBefore(EventEnricher eventEnricher);

    void clearBefore();
}
