package com.netgrif.application.engine.serialization;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.introspect.BeanPropertyDefinition;

import java.io.IOException;

/**
 * Custom JSON serializer that provides selective field serialization based on a {@link FieldSelector} configuration.
 * <p>
 * This serializer extends {@link JsonSerializer} to enable dynamic control over which fields of an object
 * are included in the JSON output. It uses a {@link FieldSelectorHolder} to access the field selector
 * configuration and applies it during serialization to filter object properties.
 * </p>
 * <p>
 * The serializer supports:
 * <ul>
 *   <li>Selective field inclusion based on field names</li>
 *   <li>Nested field selection for complex object hierarchies</li>
 *   <li>Fallback to default serialization when all fields are included</li>
 *   <li>Null-safe serialization</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * FieldSelector selector = new FieldSelector();
 * selector.include("name");
 * selector.include("address.city");
 *
 * FieldSelectorHolder holder = new FieldSelectorHolder(selector);
 * DynamicFieldSerializer serializer = new DynamicFieldSerializer(holder);
 * </pre>
 * </p>
 *
 * @see JsonSerializer
 * @see FieldSelector
 * @see FieldSelectorHolder
 */
public class DynamicFieldSerializer extends ValueSerializer<Object> {

    /**
     * Holder containing the field selector used to determine which fields should be serialized.
     */
    private final FieldSelectorHolder holder;

    /**
     * Constructs a new DynamicFieldSerializer with the specified field selector holder.
     *
     * @param holder the field selector holder that provides the selector for filtering fields during serialization
     */
    public DynamicFieldSerializer(FieldSelectorHolder holder) {
        this.holder = holder;
    }

    /**
     * Serializes the given object value to JSON using the field selector from the holder.
     * This method delegates to {@link #serializeWithSelector(Object, JsonGenerator, SerializerProvider, FieldSelector)}
     * with the selector obtained from the holder.
     *
     * @param value    the object to serialize
     * @param gen      the JSON generator used for writing JSON content
     * @param provider the serializer provider for accessing serializers
     * @throws IOException if an I/O error occurs during serialization
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext context) {
        serializeWithSelector(value, gen, context, holder.getSelector());
    }

    /**
     * Serializes the given object value to JSON based on the provided field selector.
     * <p>
     * This method performs selective serialization by including only the fields specified in the selector.
     * If the selector includes all fields, the default serialization is performed. Otherwise, it introspects
     * the object's bean properties and serializes only those included in the selector. Nested field selectors
     * are applied recursively for complex object structures.
     * </p>
     *
     * @param value    the object to serialize, it may be null
     * @param gen      the JSON generator used for writing JSON content
     * @param provider the serializer provider for accessing serializers and configuration
     * @param selector the field selector that determines which fields to include in serialization
     * @throws IOException if an I/O error occurs during serialization
     */
    public void serializeWithSelector(Object value, JsonGenerator gen, SerializationContext context, FieldSelector selector) {

        if (value == null) {
            gen.writeNull();
            return;
        }

        if (selector.includeAll()) {
            context.findValueSerializer(value.getClass()).serialize(value, gen, context);
            return;
        }

        JavaType type = context.constructType(value.getClass());
        BeanDescription desc = context.introspectBeanDescription(type);

        gen.writeStartObject();
        for (BeanPropertyDefinition prop : desc.findProperties()) {

            String name = prop.getName();
            if (!selector.includes(name)) {
                continue;
            }

            Object propValue = prop.getAccessor().getValue(value);
            gen.writeName(name);

            FieldSelector nested = selector.nested(name);
            if (nested.includeAll()) {
                context.findValueSerializer(propValue.getClass()).serialize(propValue, gen, context);
            } else {
                serializeWithSelector(propValue, gen, context, nested);
            }
        }
        gen.writeEndObject();
    }
}
