package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.domain.PdfDataGroupField;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;

import java.io.IOException;

public class DataGroupFieldRenderer extends FieldRenderer {

    public void setFieldParams(PdfField field) {
        helperField = new PdfDataGroupField(field.getFieldId(), field.getLabel(), field.getValues(), field.getType(), resource.getBaseX() + field.getX(),
                resource.getBaseY() - field.getBottomY(), field.getWidth(), field.getHeight(), resource);
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {
        setFieldParams(field);
        renderLabel(helperField, resource.getLabelFont(), resource.getFontGroupSize(), colorDataGroupLabel);
        return 0;
    }

    @Override
    public void renderValue(PdfField field, int lineCounter) throws IOException {
    }
}
