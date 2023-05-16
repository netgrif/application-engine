package com.netgrif.application.engine.mapper.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;

import java.io.IOException;

public class DataFieldValueSerializer extends StdSerializer<DataFieldValue<?>> {

    public DataFieldValueSerializer() {
        this(null);
    }

    public DataFieldValueSerializer(Class<DataFieldValue<?>> t) {
        super(t);
    }

    @Override
    public void serialize(DataFieldValue<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.getValue() instanceof I18nString) {
            gen.writeStartObject();
            gen.writeFieldName("value");
            gen.writeStartObject();
            gen.writeStringField("defaultValue", ((I18nString) value.getValue()).getDefaultValue());
            gen.writeStringField("key", ((I18nString) value.getValue()).getKey());
            gen.writeObjectField("translations", ((I18nString) value.getValue()).getTranslations());
            gen.writeEndObject();
            gen.writeEndObject();
        } else {
            gen.writeObject(value);
        }
    }
}
