package com.netgrif.workflow.pdf.generator.service.renderer;

import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.petrinet.domain.dataset.BooleanField;

import java.io.IOException;
import java.util.List;

public class BooleanFieldRenderer extends FieldRenderer<BooleanField>{

    @Override
    public void setFieldParams(PdfField field) {
        helperField = new PdfField(field.getFieldId(),field.getLabel(), field.getValues(), field.getChoices(), field.getType(),
                resource.getBaseX() + field.getX(), resource.getBaseY() - field.getBottomY(), field.getWidth(),
                field.getHeight());
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {
        setFieldParams(field);
        return renderLabel(helperField, resource.getLabelFont(), fontLabelSize);
    }
    
    @Override
    public void renderValue(PdfField field, int lineCounter) throws IOException {
        setFieldParams(field);
        renderValue(helperField, lineCounter, strokeWidth);
    }
    private void renderValue(PdfField field, int lineCounter, float strokeWidth) throws IOException {
        int x = field.getX() + padding, y = renderLinePosY(field, lineCounter);
        lineCounter++;
        lineCounter = renderPageBrake(field, lineCounter, y);
        y = renderLinePosY(field, lineCounter);
        if(resource.isBooleanFieldStroke()) {
            pdfDrawer.drawStroke(field.getX(), y, field.getBottomY(), field.getWidth(), 1, strokeWidth);
        }
        pdfDrawer.drawBooleanBox(field.getValues(), x, y);
    }
}
