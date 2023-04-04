package com.netgrif.application.engine.pdf.generator.domain.factories;

import com.netgrif.application.engine.pdf.generator.domain.fields.PdfSelectionField;

import java.util.List;
import java.util.Map;

public class PdfSelectionFieldCopier<S> extends PdfFieldCopier<Map<String, List<String>>, PdfSelectionField<S>> {

    public PdfSelectionFieldCopier(PdfSelectionField<S> field) {
        super(field);
    }

    @Override
    public PdfSelectionField<S> copyOf() {
        PdfSelectionField<S> copy = super.copyOf();
        copy.setSelectedValues(getField().getSelectedValues());
        return copy;
    }
}
