package com.netgrif.application.engine.petrinet.domain.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import static com.netgrif.application.engine.petrinet.domain.version.Version.LATEST;
import static com.netgrif.application.engine.petrinet.domain.version.Version.NEWEST;

@Component
@ReadingConverter
public class StringToVersionConverter implements Converter<String, Version> {

    public static final Logger log = LoggerFactory.getLogger(StringToVersionConverter.class);

    @Override
    public Version convert(String source) {
        if (NEWEST.equals(source) || LATEST.equals(source)) {
            return null;
        }
        return Version.from(source);
    }
}