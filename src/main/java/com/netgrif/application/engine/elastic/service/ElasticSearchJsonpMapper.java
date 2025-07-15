package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.netgrif.application.engine.elastic.serializer.LocalDateTimeJsonDeserializer;
import com.netgrif.application.engine.elastic.serializer.LocalDateTimeJsonSerializer;

import java.time.LocalDateTime;

public class ElasticSearchJsonpMapper extends JacksonJsonpMapper {
    public ElasticSearchJsonpMapper() {
        super(configureMapper());
    }

    private static ObjectMapper configureMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
        mapper.registerModule(javaTimeModule);

        return mapper;
    }
}
