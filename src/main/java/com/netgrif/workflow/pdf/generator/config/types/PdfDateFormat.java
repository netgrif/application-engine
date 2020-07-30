package com.netgrif.workflow.pdf.generator.config.types;

import lombok.Getter;

public enum PdfDateFormat {

    SLOVAK1("dd-MM-yyyy"),
    SLOVAK2("dd.MM.yyyy"),
    US1("MM-dd-yyyy"),
    US2("MM.dd.yyyy"),
    ;

    @Getter
    private String value;

    PdfDateFormat(String s) {
        value = s;
    }
}
