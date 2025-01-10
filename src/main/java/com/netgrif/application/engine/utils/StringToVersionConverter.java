package com.netgrif.application.engine.utils;

import com.netgrif.application.engine.workflow.domain.Version;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import static com.netgrif.application.engine.workflow.domain.Version.LATEST;

@Slf4j
@Component
@ReadingConverter
public class StringToVersionConverter implements Converter<String, Version> {

    @Override
    public Version convert(@NonNull String source) {
        if (LATEST.equals(source)) {
            return null;
        }

        Version version = new Version();
        try {
            String[] split = source.split("\\.");
            version.setMajor(Long.parseLong(split[0]));
            version.setMinor(Long.parseLong(split[1]));
            version.setPatch(Long.parseLong(split[2]));
        } catch (Exception e) {
            log.error("Could not parse version {} caused by:", source, e);
        }

        return version;
    }
}