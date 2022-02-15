package com.netgrif.application.engine.event.publishers;

import com.netgrif.application.engine.event.events.Event;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Publisher for NAE Event system.
 * */
@Slf4j
public abstract class NaeEventPublisher {

    @Getter
    @Setter
    private ApplicationEventPublisher applicationEventPublisher;

    protected NaeEventPublisher() {
    }

    protected NaeEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Publishes event to application context.
     * @param event - the NaeEvent instance (can be extended class), that contains the source object;
     * */
    public void publish(Event event) {
        log.info("Publishing event " + event.getTimestamp());
        this.applicationEventPublisher.publishEvent(event);
    }
}
