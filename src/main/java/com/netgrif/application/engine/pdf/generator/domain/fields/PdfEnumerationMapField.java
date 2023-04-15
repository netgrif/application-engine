package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfSelectionFieldCopier;

import java.util.Set;

public class PdfEnumerationMapField extends PdfSelectionField<Set<String>>{

    public PdfEnumerationMapField() {
        super();
    }

    public PdfEnumerationMapField(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return DataType.ENUMERATION_MAP.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null || value.isEmpty();
    }

    @Override
    public PdfEnumerationMapField newInstance() {
        return new PdfEnumerationMapField();
    }

    @Override
    public PdfSelectionFieldCopier<Set<String>> getCopier() {
        return new PdfSelectionFieldCopier<>(this);
    }
}
