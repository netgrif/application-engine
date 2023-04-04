package com.netgrif.application.engine.pdf.generator.config.types;

import lombok.Getter;

import java.util.*;

public enum PdfBooleanFormat {
    BOX_WITH_TEXT_SK(new Locale("SK"), Map.of(Boolean.TRUE, "Áno", Boolean.FALSE, "Nie")),
    BOX_WITH_TEXT_EN(Locale.ENGLISH, Map.of(Boolean.TRUE, "Yes", Boolean.FALSE, "No")),
    BOX_WITH_TEXT_DE(Locale.GERMAN, Map.of(Boolean.TRUE, "Ja", Boolean.FALSE, "Nein")),
    ;

    @Getter
    private final Locale locale;

    @Getter
    private final Map<Boolean, String> value;

    PdfBooleanFormat(Locale locale, Map<Boolean, String> value) {
        this.locale = locale;
        this.value = value;
    }

    public static PdfBooleanFormat getByLocale(Locale locale) {
        return Arrays.stream(values()).filter(v -> v.locale == locale).findFirst().orElse(BOX_WITH_TEXT_EN);
    }
}
