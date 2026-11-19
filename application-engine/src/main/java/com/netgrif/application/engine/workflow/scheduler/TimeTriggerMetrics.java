package com.netgrif.application.engine.workflow.scheduler;

import com.netgrif.application.engine.workflow.service.TimeTriggerOutcome;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TimeTriggerMetrics {

    private final MeterRegistry registry;

    public TimeTriggerMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void scheduled() {
        registry.counter("netgrif.engine.time_trigger.scheduled").increment();
    }

    public void executed(TimeTriggerOutcome outcome) {
        registry.counter("netgrif.engine.time_trigger.executed", "outcome", outcome.name().toLowerCase()).increment();
    }

    public void failed() {
        registry.counter("netgrif.engine.time_trigger.failed").increment();
    }

    public void scheduleFailed() {
        registry.counter("netgrif.engine.time_trigger.schedule_failed").increment();
    }

    public void redelivered() {
        registry.counter("netgrif.engine.time_trigger.redelivered").increment();
    }

    public void lateness(long millis) {
        registry.timer("netgrif.engine.time_trigger.lateness").record(millis, TimeUnit.MILLISECONDS);
    }
}
