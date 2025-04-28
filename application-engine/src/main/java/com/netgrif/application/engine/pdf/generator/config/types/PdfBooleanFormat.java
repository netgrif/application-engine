package com.netgrif.application.engine.pdf.generator.config.types;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PdfBooleanFormat {

    SINGLE_BOX_SK(Collections.singletonList("")),
    SINGLE_BOX_WITH_TEXT_SK(Collections.singletonList("Áno")),
    DOUBLE_BOX_WITH_TEXT_SK(Arrays.asList("Áno", "Nie")),
    SINGLE_BOX_EN(Collections.singletonList("")),
    SINGLE_BOX_WITH_TEXT_EN(Collections.singletonList("Yes")),
    DOUBLE_BOX_WITH_TEXT_EN(Arrays.asList("Yes", "No")),
    ;

    @Getter
    private final List<String> value;

    PdfBooleanFormat(List<String> s) {
        value = s;
    }
}
