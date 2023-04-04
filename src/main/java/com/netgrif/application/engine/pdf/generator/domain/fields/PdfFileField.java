package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;
import org.springframework.security.core.parameters.P;

import java.util.List;

public class PdfFileField extends PdfField<List<String>> {

    public PdfFileField() {
        super();
    }

    public PdfFileField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.FILE.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null || value.isEmpty();
    }

    @Override
    public PdfField<List<String>> newInstance() {
        return new PdfFileField();
    }
}
