package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.importer.model.LayoutType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ReadingConverter
public class LayoutTypeConverter implements Converter<String, LayoutType> {

    @Override
    public LayoutType convert(String source) {
        if (source.isBlank()) {
            return null;
        }
        return LayoutType.fromValue(source.toLowerCase());
    }
}
