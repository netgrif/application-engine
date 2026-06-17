package com.netgrif.application.engine.configuration.groovy.converter;

import org.codehaus.groovy.runtime.GStringImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class GStringToStringConverter implements Converter<GStringImpl, String> {
    @Override
    public String convert(GStringImpl source) {
        return source.toString();
    }
}
