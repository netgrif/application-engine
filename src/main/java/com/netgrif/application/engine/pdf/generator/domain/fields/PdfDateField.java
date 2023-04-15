package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;

public class PdfDateField extends PdfField<String> {

    public PdfDateField() {
    }

    public PdfDateField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.DATE.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null;
    }

    @Override
    public PdfField<String> newInstance() {
        return new PdfDateField();
    }
}
