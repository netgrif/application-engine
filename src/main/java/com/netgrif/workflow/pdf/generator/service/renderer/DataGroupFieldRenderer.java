package com.netgrif.workflow.pdf.generator.service.renderer;

import com.netgrif.workflow.pdf.generator.domain.PdfField;
import java.io.IOException;

public class DataGroupFieldRenderer extends FieldRenderer<TextFieldRenderer> {

    @Override
    public void setFieldParams(PdfField field) {
        helperField = new PdfField(field.getFieldId(),field.getLabel(), field.getValues(), field.getChoices(), field.getType(), resource.getBaseX() + field.getX(),
                resource.getBaseY() - field.getBottomY(), field.getWidth(), field.getHeight());
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {
        setFieldParams(field);
        renderLabel(helperField, resource.getLabelFont(), resource.getFontGroupSize());
        return 0;
    }

    @Override
    public void renderValue(PdfField field, int lineCounter) throws IOException {}
}
