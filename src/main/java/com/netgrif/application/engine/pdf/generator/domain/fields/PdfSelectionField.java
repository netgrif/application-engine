package com.netgrif.application.engine.pdf.generator.domain.fields;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public abstract class PdfSelectionField<S> extends PdfField<Map<String, List<String>>> {

    public static final String LIST_COMPONENT_NAME = "list";

    @Getter
    @Setter
    private S selectedValues;

    public PdfSelectionField(S selectedValues) {
        super();
        this.selectedValues = selectedValues;
    }

    public PdfSelectionField(String id) {
        super(id);
    }
}
