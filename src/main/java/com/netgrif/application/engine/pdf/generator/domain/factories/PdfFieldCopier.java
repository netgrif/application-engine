package com.netgrif.application.engine.pdf.generator.domain.factories;

import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import lombok.Getter;

public class PdfFieldCopier<U, T extends PdfField<U>> implements IPdfFieldCopier<U, T> {

    @Getter
    private final T field;

    public PdfFieldCopier(T field) {
        this.field = field;
    }

    public T copyOf() {
        return IPdfFieldCopier.super.copyOf(field);
    }
}
