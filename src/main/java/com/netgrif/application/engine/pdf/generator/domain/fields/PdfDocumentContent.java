package com.netgrif.application.engine.pdf.generator.domain.fields;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PdfDocumentContent {

    private PdfTitleField titleField;

    private List<PdfField<?>> pdfFormFields;

    public PdfDocumentContent() {
        this.pdfFormFields = new ArrayList<>();
    }

    public void addFormField(PdfField<?> pdfField) {
        if (pdfFormFields != null) {
            pdfFormFields.add(pdfField);
        }
    }
}
