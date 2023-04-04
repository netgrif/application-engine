package com.netgrif.application.engine.pdf.generator.domain.fields;

public class PdfPageNumberField extends PdfField<Integer> {

    public static final String PAGE_NUMBER_TYPE = "pageNumber";

    public PdfPageNumberField() {
    }

    public PdfPageNumberField(String fieldId) {
        super(fieldId);
    }

    @Override
    public String getType() {
        return PAGE_NUMBER_TYPE;
    }

    @Override
    public boolean isValueEmpty() {
        return value == null;
    }

    @Override
    public PdfField<Integer> newInstance() {
        return new PdfPageNumberField();
    }

    public void increase() {
        this.value += 1;
    }
}
