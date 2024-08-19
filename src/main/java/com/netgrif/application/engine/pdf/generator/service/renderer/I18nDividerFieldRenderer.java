package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfTextField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilder.FieldBuilder;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class I18nDividerFieldRenderer extends FieldRenderer {

    public void setFieldParams(PdfField field) {
        helperField = new PdfTextField(field.getFieldId(), field.getLabel(), field.getValues(), field.getType(),
                resource.getBaseX() + field.getX(), resource.getBaseY() - field.getBottomY(), field.getWidth(), field.getHeight(), resource);
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {
        return 0;
    }

    @Override
    public void renderValue(PdfField field, int lineCounter) throws IOException {
        setFieldParams(field);
        renderValue(helperField, resource.getLabelFont(), resource.getFontGroupSize(), colorDataGroupLabel);
    }

    private void renderValue(PdfField field, PDType0Font font, int fontSize, Color colorLabel) throws IOException {
        float textWidth = getTextWidth(Collections.singletonList(field.getLabel()), font, fontSize, resource);
        int maxLineSize = getMaxLabelLineSize(field.getWidth(), fontSize);
        List<String> multiLineText = new ArrayList<>(field.getValues());
        int linesOnPage = 0;
        int x = field.getX() + padding, y = renderLinePosY(field, 1);

        if (textWidth > field.getWidth() - padding) {
            multiLineText = FieldBuilder.generateMultiLineText(field.getValues(), maxLineSize);
        }

        for (String line : multiLineText) {
            linesOnPage++;
            linesOnPage = renderPageBrake(field, linesOnPage, y);
            y = renderLinePosY(field, linesOnPage);
            pdfDrawer.writeString(font, fontSize, x, y, line, colorLabel);
        }
        pdfDrawer.checkOpenPages();
    }
}
