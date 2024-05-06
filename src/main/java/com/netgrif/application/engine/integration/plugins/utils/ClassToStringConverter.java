package com.netgrif.application.engine.integration.plugins.utils;

import com.mongodb.lang.NonNull;
import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.mongodb.core.convert.MongoConversionContext;

import java.util.List;
import java.util.stream.Collectors;

public class ClassToStringConverter implements PropertyValueConverter<List<Class<?>>, List<String>, MongoConversionContext> {

    /**
     * Maps provided class names into list of {@link Class} objects
     * */
    @Override
    public List<Class<?>> read(List<String> classNames, @NonNull MongoConversionContext context) {
        return classNames.stream().map(name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Maps provided objects of {@link Class} into list of class names
     * */
    @Override
    public List<String> write(List<Class<?>> value, @NonNull MongoConversionContext context) {
        return value.stream().map(Class::getName).collect(Collectors.toList());
    }
}