package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;

public class PdfNumberField extends PdfField<String> {

    public PdfNumberField() {
        super();
    }

    public PdfNumberField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.NUMBER.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null;
    }

    @Override
    public PdfNumberField newInstance() {
        return new PdfNumberField();
    }
}
