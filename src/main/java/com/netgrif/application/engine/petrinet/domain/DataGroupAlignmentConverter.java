package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.importer.model.DataGroupAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ReadingConverter
public class DataGroupAlignmentConverter implements Converter<String, DataGroupAlignment> {

    @Override
    public DataGroupAlignment convert(String source) {
        if (source.isBlank()) {
            return null;
        }
        return DataGroupAlignment.fromValue(source.toLowerCase());
    }
}
