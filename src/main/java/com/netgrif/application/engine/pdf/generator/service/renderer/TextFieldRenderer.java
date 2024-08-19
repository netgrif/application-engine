package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfTextField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilder.FieldBuilder;

import java.io.IOException;
import java.util.List;

public class TextFieldRenderer extends FieldRenderer {

    public void setFieldParams(PdfField field) {
        helperField = new PdfTextField(field.getFieldId(), field.getLabel(), field.getValues(), field.getType(),
                resource.getBaseX() + field.getX(), resource.getBaseY() - field.getBottomY(), field.getWidth(), field.getHeight(), resource);
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
        float textWidth = getTextWidth(field.getValues(), resource.getValueFont(), fontValueSize, resource);
        int maxLineSize = getMaxValueLineSize(field.getWidth() - 3 * padding);
        List<String> multiLineText = field.getValues();
        int x = field.getX() + padding, y = renderLinePosY(field, lineCounter);
        int strokeLineCounter = 0;

        if (textWidth > field.getWidth() - 3 * padding) {
            multiLineText = FieldBuilder.generateMultiLineText(field.getValues(), maxLineSize);
        }

        for (String line : multiLineText) {
            lineCounter++;
            lineCounter = renderPageBrake(field, lineCounter, strokeLineCounter, y);
            strokeLineCounter = lineCounter == 1 ? 0 : strokeLineCounter;
            y = renderLinePosY(field, lineCounter);
            strokeLineCounter++;
            pdfDrawer.writeString(resource.getValueFont(), fontValueSize, x, y, line, colorString);
        }
        if (resource.isTextFieldStroke()) {
            pdfDrawer.drawStroke(field.getX(), y, field.getBottomY(), field.getWidth(), strokeLineCounter, strokeWidth);
        }
        pdfDrawer.checkOpenPages();
    }

}
