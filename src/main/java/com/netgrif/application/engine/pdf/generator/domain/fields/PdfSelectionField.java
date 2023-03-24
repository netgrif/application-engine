package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.pdf.generator.domain.factories.PdfFieldCopier;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfSelectionFieldCopier;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public abstract class PdfSelectionField<S> extends PdfField<Map<String, List<String>>> {

    @Getter
    @Setter
    protected S selectedValues;

    public PdfSelectionField() {
        super();
    }

    public PdfSelectionField(String id) {
        super(id);
    }
}
