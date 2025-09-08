package com.netgrif.application.engine.petrinet.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
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
        if (value == null || value.getValue() == null) {
            provider.defaultSerializeNull(gen);
            return;
        }
        provider.defaultSerializeValue(value.getValue(), gen);
    }
}
