package com.netgrif.application.engine.petrinet.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.netgrif.application.engine.workflow.domain.I18nString;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Locale;

public class I18nStringSerializer extends StdSerializer<I18nString> {

    public I18nStringSerializer() {
        this(null);
    }

    public I18nStringSerializer(Class<I18nString> t) {
        super(t);
    }

    @Override
    public void serialize(I18nString value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Locale locale = LocaleContextHolder.getLocale();
        String translation = value.getTranslation(locale);
        gen.writeString(translation);
    }
}