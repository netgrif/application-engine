package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfSelectionFieldCopier;

import java.util.HashSet;
import java.util.Set;

public class PdfMultiChoiceField extends PdfSelectionField<Set<String>> {

    public PdfMultiChoiceField() {
        super(new HashSet<>());
    }

    public PdfMultiChoiceField(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return DataType.MULTICHOICE.value();
    }

    @Override
    public boolean isValueEmpty() {
        return this.value == null || this.value.isEmpty();
    }

    @Override
    public PdfMultiChoiceField newInstance() {
        return new PdfMultiChoiceField();
    }

    @Override
    public PdfSelectionFieldCopier<Set<String>> getCopier() {
        return new PdfSelectionFieldCopier<>(this);
    }
}
