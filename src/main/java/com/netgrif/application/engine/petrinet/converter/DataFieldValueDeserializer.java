package com.netgrif.application.engine.petrinet.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor
public class DataFieldValueDeserializer extends JsonDeserializer<DataFieldValue<?>> implements ContextualDeserializer {

    private JavaType type;

    private DataFieldValueDeserializer(JavaType type) {
        this();
        this.type = type;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType wrapperType = property.getType().containedType(0);
        return new DataFieldValueDeserializer(wrapperType);
    }

    @Override
    public DataFieldValue<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        DataFieldValue<?> wrapper = new DataFieldValue<>();
        wrapper.setValue(deserializationContext.readValue(jsonParser, type));
        return wrapper;
    }
}
