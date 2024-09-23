package com.netgrif.application.engine.importer.service.trigger;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TriggerFactory {

    private final Map<TriggerType, TriggerBuilder> builders;

    @Autowired
    public TriggerFactory(List<TriggerBuilder> builders) {
        this.builders = builders.stream().collect(Collectors.toMap(TriggerBuilder::getType, Function.identity()));
    }

    public Trigger buildTrigger(com.netgrif.application.engine.importer.model.Trigger trigger) throws IllegalArgumentException, DateTimeParseException {
        TriggerBuilder builder = builders.get(trigger.getType());
        if (builder == null) {
            throw new IllegalArgumentException(trigger.getType() + " is not a valid Trigger type");
        }
        return builder.build(trigger);
    }
}