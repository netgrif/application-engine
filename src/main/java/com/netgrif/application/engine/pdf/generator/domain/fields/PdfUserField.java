package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;

public class PdfUserField extends PdfField<String> {

    public PdfUserField() {
    }

    public PdfUserField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.USER.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null;
    }

    @Override
    public PdfField<String> newInstance() {
        return new PdfUserField();
    }
}
