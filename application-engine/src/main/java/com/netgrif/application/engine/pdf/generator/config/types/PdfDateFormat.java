package com.netgrif.application.engine.pdf.generator.config.types;

import lombok.Getter;

public enum PdfDateFormat {

    SLOVAK1("dd-MM-yyyy"),
    SLOVAK2("dd.MM.yyyy"),
    SLOVAK1_DATETIME("dd-MM-yyyy HH:mm:ss"),
    SLOVAK2_DATETIME("dd.MM.yyyy HH:mm:ss"),
    US1("MM-dd-yyyy"),
    US2("MM.dd.yyyy"),
    US1_DATETIME("MM-dd-yyyy HH:mm:ss"),
    US2_DATETIME("MM.dd.yyyy HH:mm:ss"),
    ;

    @Getter
    private String value;

    PdfDateFormat(String s) {
        value = s;
    }
}
