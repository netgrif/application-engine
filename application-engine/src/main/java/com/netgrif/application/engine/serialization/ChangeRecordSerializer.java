package com.netgrif.application.engine.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.LocalisedGetDataEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.LocalisedGetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.LocalisedSetDataEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedCaseEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.io.IOException;

public class ChangeRecordSerializer extends JsonSerializer<LocalisedEventOutcome> {

    private final FieldSelectorHolder selectorHolder;

    public ChangeRecordSerializer(FieldSelectorHolder selectorHolder) {
        this.selectorHolder = selectorHolder;
    }

    @Override
    public void serialize(LocalisedEventOutcome outcome, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        FieldSelector selector = selectorHolder.getSelector();

        if (selector.includes("message") && outcome.getMessage() != null) {
            gen.writeObjectField("message", outcome.getMessage());
        }
        if (selector.includes("frontActions") && outcome.getFrontActions() != null) {
            gen.writeObjectField("frontActions", outcome.getFrontActions());
        }
        if (selector.includes("outcomes")
                && outcome.getOutcomes() != null) {
            gen.writeArrayFieldStart("outcomes");
            for (LocalisedEventOutcome child : outcome.getOutcomes()) {
                provider.defaultSerializeValue(child, gen);
            }
            gen.writeEndArray();
        }
        if (outcome instanceof LocalisedPetriNetEventOutcome specificOutcome && selector.includes("net") && specificOutcome.getNet() != null) {
            gen.writeObjectField("net", specificOutcome.getNet());
        }
        if (outcome instanceof LocalisedCaseEventOutcome specificOutcome && selector.includes("case") && specificOutcome.getaCase() != null) {
            gen.writeObjectField("aCase", specificOutcome.getaCase());
        }
        if (outcome instanceof LocalisedTaskEventOutcome specificOutcome && selector.includes("task") && specificOutcome.getTask() != null) {
            gen.writeObjectField("task", specificOutcome.getaCase());
        }
        if (outcome instanceof LocalisedGetDataEventOutcome specificOutcome && selector.includes("data") && specificOutcome.getData() != null) {
            gen.writeObjectField("data", specificOutcome.getData());
        }
        if (outcome instanceof LocalisedGetDataGroupsEventOutcome specificOutcome && selector.includes("data") && specificOutcome.getData() != null) {
            gen.writeObjectField("data", specificOutcome.getData());
        }
        if (outcome instanceof LocalisedSetDataEventOutcome specificOutcome && selector.includes("changedFields") && specificOutcome.getChangedFields() != null) {
            gen.writeObjectField("changedFields", specificOutcome.getChangedFields());
        }

        gen.writeEndObject();
    }
}
