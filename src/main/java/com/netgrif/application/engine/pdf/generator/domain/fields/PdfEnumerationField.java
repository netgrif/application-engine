package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfFieldCopier;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfSelectionFieldCopier;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PdfEnumerationField extends PdfSelectionField<Set<String>> {

    public PdfEnumerationField() {
        super();
    }

    public PdfEnumerationField(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return DataType.ENUMERATION.value();
    }

    @Override
    public boolean isValueEmpty() {
        return this.value == null || this.value.isEmpty();
    }

    @Override
    public PdfEnumerationField newInstance() {
        return new PdfEnumerationField();
    }

    @Override
    public PdfSelectionFieldCopier<Set<String>> getCopier() {
        return new PdfSelectionFieldCopier<>(this);
    }
}
