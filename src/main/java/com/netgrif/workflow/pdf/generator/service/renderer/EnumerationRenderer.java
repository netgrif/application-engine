package com.netgrif.workflow.pdf.generator.service.renderer;

import com.netgrif.workflow.pdf.generator.domain.PdfEnumerationField;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfSelectionField;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationField;

import java.io.IOException;

public class EnumerationRenderer extends SelectionFieldRenderer<EnumerationField> {

    public void setFieldParams(PdfEnumerationField field) {
        helperField = new PdfEnumerationField(field.getFieldId(),field.getLabel(), field.getValues(), field.getChoices(), field.getType(), resource.getBaseX() + field.getX(),
                resource.getBaseY() - field.getBottomY(), field.getWidth(), field.getHeight(), resource);
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {
        setFieldParams((PdfEnumerationField) field);
        return renderLabel(helperField, resource.getLabelFont(), fontLabelSize);
    }

    public void renderValue(PdfField field, int lineCounter) throws IOException {
        setFieldParams((PdfEnumerationField) field);
        renderValue((PdfSelectionField) helperField, lineCounter);
    }
}
