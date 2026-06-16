package com.netgrif.application.engine.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import java.io.IOException;

public class DynamicFieldSerializer extends JsonSerializer<Object> {

    private final FieldSelectorHolder holder;

    public DynamicFieldSerializer(FieldSelectorHolder holder) {
        this.holder = holder;
    }

    @Override
    public Class<Object> handledType() {
        return Object.class;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        serializeWithSelector(value, gen, provider, holder.getSelector());
    }

    public void serializeWithSelector(Object value, JsonGenerator gen,
                                      SerializerProvider provider, FieldSelector selector) throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        if (selector.includeAll()) {
            provider.defaultSerializeValue(value, gen);
            return;
        }

        JavaType type = provider.constructType(value.getClass());
        BeanDescription desc = provider.getConfig().introspect(type);

        gen.writeStartObject();
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            String name = prop.getName();
            if (!selector.includes(name)) continue;

            Object propValue = prop.getAccessor().getValue(value);
            gen.writeFieldName(name);

            FieldSelector nested = selector.nested(name);
            if (nested.includeAll()) {
                provider.defaultSerializeValue(propValue, gen);
            } else {
                serializeWithSelector(propValue, gen, provider, nested);
            }
        }
        gen.writeEndObject();
    }
}
