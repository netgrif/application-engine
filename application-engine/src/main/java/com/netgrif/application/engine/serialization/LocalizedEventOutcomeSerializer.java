package com.netgrif.application.engine.serialization;

import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.LocalisedGetDataEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.LocalisedGetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.LocalisedSetDataEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedCaseEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.io.IOException;

/**
 * Custom JSON serializer for {@link LocalisedEventOutcome} objects that provides selective field serialization
 * based on a {@link FieldSelector} configuration.
 * <p>
 * This serializer handles various types of localized event outcomes including:
 * <ul>
 *   <li>{@link LocalisedPetriNetEventOutcome} - outcomes related to Petri nets</li>
 *   <li>{@link LocalisedCaseEventOutcome} - outcomes related to cases</li>
 *   <li>{@link LocalisedTaskEventOutcome} - outcomes related to tasks</li>
 *   <li>{@link LocalisedGetDataEventOutcome} - outcomes for data retrieval operations</li>
 *   <li>{@link LocalisedGetDataGroupsEventOutcome} - outcomes for data groups retrieval</li>
 *   <li>{@link LocalisedSetDataEventOutcome} - outcomes for data modification operations</li>
 * </ul>
 * The serializer uses a {@link DynamicFieldSerializer} to handle nested object serialization with field selection support.
 * </p>
 *
 * @see LocalisedEventOutcome
 * @see FieldSelector
 * @see DynamicFieldSerializer
 */
@JacksonComponent(type = LocalisedEventOutcome.class)
public class LocalizedEventOutcomeSerializer extends ValueSerializer<LocalisedEventOutcome> {

    /**
     * Holder containing the {@link FieldSelector} that determines which fields should be included
     * in the serialized JSON output.
     */
    private final FieldSelectorHolder selectorHolder;

    /**
     * Serializer used for dynamic field serialization of nested objects with field selection support.
     */
    private final DynamicFieldSerializer dynamicSerializer;

    /**
     * Constructs a new LocalizedEventOutcomeSerializer with the specified field selector holder.
     *
     * @param selectorHolder the holder containing the field selector configuration for controlling
     *                       which fields are included in the serialized output
     */
    public LocalizedEventOutcomeSerializer(FieldSelectorHolder selectorHolder) {
        this.selectorHolder = selectorHolder;
        this.dynamicSerializer = new DynamicFieldSerializer(selectorHolder);
    }

    /**
     * Serializes a {@link LocalisedEventOutcome} object to JSON format with selective field inclusion
     * based on the configured {@link FieldSelector}.
     * <p>
     * The method handles serialization of common fields (message, frontActions, outcomes) and type-specific
     * fields based on the actual runtime type of the outcome object. Nested objects are serialized using
     * the {@link DynamicFieldSerializer} with their corresponding nested field selectors.
     * </p>
     *
     * @param outcome  the localized event outcome object to serialize
     * @param gen      the JSON generator used to write JSON content
     * @param provider the serializer provider for accessing additional serializers
     * @throws IOException if an I/O error occurs during serialization
     */
    @Override
    public void serialize(LocalisedEventOutcome outcome, JsonGenerator gen, SerializationContext context) {
        gen.writeStartObject();
        FieldSelector selector = selectorHolder.getSelector();

        if (selector.includes("message") && outcome.getMessage() != null) {
            gen.writePOJOProperty("message", outcome.getMessage());
        }
        if (selector.includes("frontActions") && outcome.getFrontActions() != null) {
            gen.writePOJOProperty("frontActions", outcome.getFrontActions());
        }
        if (selector.includes("outcomes")
                && outcome.getOutcomes() != null) {
            gen.writeArrayPropertyStart("outcomes");
            for (LocalisedEventOutcome child : outcome.getOutcomes()) {
                context.findValueSerializer(child.getClass()).serialize(child, gen, context);
            }
            gen.writeEndArray();
        }
        if (outcome instanceof LocalisedPetriNetEventOutcome specificOutcome && selector.includes("net") && specificOutcome.getNet() != null) {
            gen.writeName("net");
            dynamicSerializer.serializeWithSelector(specificOutcome.getNet(), gen, context, selector.nested("net"));
        }
        if (outcome instanceof LocalisedCaseEventOutcome specificOutcome && selector.includes("case") && specificOutcome.getaCase() != null) {
            gen.writeName("aCase");
            dynamicSerializer.serializeWithSelector(specificOutcome.getaCase(), gen, context, selector.nested("case"));
        }
        if (outcome instanceof LocalisedTaskEventOutcome specificOutcome && selector.includes("task") && specificOutcome.getTask() != null) {
            gen.writeName("task");
            dynamicSerializer.serializeWithSelector(specificOutcome.getTask(), gen, context, selector.nested("task"));
        }
        if (outcome instanceof LocalisedGetDataEventOutcome specificOutcome && selector.includes("data") && specificOutcome.getData() != null) {
            gen.writeName("data");
            dynamicSerializer.serializeWithSelector(specificOutcome.getData(), gen, context, selector.nested("data"));
        }
        if (outcome instanceof LocalisedGetDataGroupsEventOutcome specificOutcome && selector.includes("data") && specificOutcome.getData() != null) {
            gen.writeName("data");
            dynamicSerializer.serializeWithSelector(specificOutcome.getData(), gen, context, selector.nested("data"));
        }
        if (outcome instanceof LocalisedSetDataEventOutcome specificOutcome && selector.includes("changedFields") && specificOutcome.getChangedFields() != null) {
            gen.writeName("changedFields");
            dynamicSerializer.serializeWithSelector(specificOutcome.getChangedFields(), gen, context, selector.nested("changedFields"));
        }

        gen.writeEndObject();
    }
}
