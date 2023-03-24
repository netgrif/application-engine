package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfFieldCopier;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfSelectionFieldCopier;

import java.util.List;
import java.util.Map;

public class PdfEnumerationField extends PdfSelectionField<String> {

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
    public PdfSelectionFieldCopier<String> getCopier() {
        return new PdfSelectionFieldCopier<>(this);
    }
}
