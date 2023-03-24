package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;

import java.util.List;

public class PdfFileListField extends PdfField<List<String>> {

    public PdfFileListField() {
    }

    public PdfFileListField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.FILE_LIST.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null || value.isEmpty();
    }

    @Override
    public PdfField<List<String>> newInstance() {
        return new PdfFileListField();
    }
}
