package com.netgrif.application.engine.integration.plugins.utils;

import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.mongodb.core.convert.MongoConversionContext;

import java.util.List;
import java.util.stream.Collectors;

public class ClassToStringConverter implements PropertyValueConverter<List<Class<?>>, List<String>, MongoConversionContext> {
    @Override
    public List<Class<?>> read(List<String> value, MongoConversionContext context) {
        return value.stream().map(v -> {
            try {
                return Class.forName(v);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> write(List<Class<?>> value, MongoConversionContext context) {
        return value.stream().map(Class::getName).collect(Collectors.toList());
    }
}