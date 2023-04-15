package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;

import java.util.List;

public class PdfI18nDividerField extends PdfField<List<String>> {

    public PdfI18nDividerField() {
        super();
    }

    public PdfI18nDividerField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.I_18_N.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null || value.isEmpty();
    }

    @Override
    public PdfI18nDividerField newInstance() {
        return new PdfI18nDividerField();
    }
}
