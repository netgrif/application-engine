package com.netgrif.application.engine.pdf.generator.domain.factories;

import com.netgrif.application.engine.pdf.generator.domain.fields.PdfBooleanField;

public class PdfBooleanFieldCopier extends PdfFieldCopier<Boolean, PdfBooleanField> {

    public PdfBooleanFieldCopier(PdfBooleanField field) {
        super(field);
    }

    @Override
    public PdfBooleanField copyOf() {
        PdfBooleanField copy = super.copyOf();
        copy.setFormat(getField().getFormat());
        return copy;
    }
}
