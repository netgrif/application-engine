package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;

import java.util.List;

public class PdfUserListField extends PdfField<List<String>> {

    public PdfUserListField() {
    }

    public PdfUserListField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.USER_LIST.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null || value.isEmpty();
    }

    @Override
    public PdfField<List<String>> newInstance() {
        return new PdfUserListField();
    }
}
