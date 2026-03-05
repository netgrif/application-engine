package com.netgrif.application.engine.adapter.spring.event;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Primary
@Component
public class NetgrifEventPublisherImpl implements NetgrifEventPublisher {

    private final ApplicationContext applicationContext;
    @Getter
    private final List<EventEnricher> before;

    public NetgrifEventPublisherImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.before = new ArrayList<>();
    }

    @Override
    public void publishEvent(Object event) {
        Object transformedBefore = before.stream().reduce(event, (e, enricher) -> enricher.enrich(e), (e1, e2) -> e1);
        applicationContext.publishEvent(transformedBefore);
    }

    @Override
    public void addBeforePublish(EventEnricher eventEnricher) {
        before.add(eventEnricher);
    }

    @Override
    public void removeBefore(EventEnricher eventEnricher) {
        before.remove(eventEnricher);
    }

    @Override
    public void clearBefore() {
        before.clear();
    }

}