package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.config.types.PdfBooleanFormat;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfTextField;
import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;

import java.io.IOException;
import java.util.List;

public class BooleanFieldRenderer extends FieldRenderer {

    private PdfBooleanFormat booleanFormat;

    public void setFieldParams(PdfField field) {
        helperField = new PdfTextField(field.getFieldId(), field.getLabel(), field.getValues(), field.getType(),
                resource.getBaseX() + field.getX(), resource.getBaseY() - field.getBottomY(), field.getWidth(),
                field.getHeight(), resource);
        booleanFormat = resource.getBooleanFormat();
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {
        setFieldParams(field);
        return renderLabel(helperField, resource.getLabelFont(), fontLabelSize, colorLabelString);
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
        if (resource.isBooleanFieldStroke()) {
            pdfDrawer.drawStroke(field.getX(), y, field.getBottomY(), field.getWidth(), 1, strokeWidth);
        }

        List<String> booleanValues = booleanFormat.getValue();
        for (String value : booleanValues) {
            x += booleanValues.indexOf(value) * (padding * 9);
            pdfDrawer.drawBooleanBox(field.getValues(), value, x, y);
            pdfDrawer.writeString(resource.getValueFont(), resource.getFontValueSize(), x + fontLabelSize + padding, y, value, colorString);
        }
        pdfDrawer.checkOpenPages();
    }
}
