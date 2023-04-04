package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.config.types.PdfBooleanFormat;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfBooleanFieldCopier;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfFieldCopier;
import lombok.Getter;
import lombok.Setter;


public class PdfBooleanField extends PdfField<Boolean> {

    @Getter
    @Setter
    private PdfBooleanFormat format;

    public PdfBooleanField() {
        super();
    }

    public PdfBooleanField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return DataType.BOOLEAN.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null;
    }

    @Override
    public PdfBooleanField newInstance() {
        return new PdfBooleanField();
    }

    @Override
    public PdfFieldCopier<Boolean, ?> getCopier() {
        return new PdfBooleanFieldCopier(this);
    }
}
