package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;

public class PdfDateTimeField extends PdfField<String> {

    public PdfDateTimeField() {
    }

    public PdfDateTimeField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.DATE_TIME.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null;
    }

    @Override
    public PdfField<String> newInstance() {
        return new PdfDateTimeField();
    }
}
