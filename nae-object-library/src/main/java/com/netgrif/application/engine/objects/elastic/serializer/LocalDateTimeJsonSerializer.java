package com.netgrif.application.engine.objects.elastic.serializer;


import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeJsonSerializer extends ValueSerializer<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext serializers) {
        gen.writeString(FORMATTER.format(value));
    }
}
