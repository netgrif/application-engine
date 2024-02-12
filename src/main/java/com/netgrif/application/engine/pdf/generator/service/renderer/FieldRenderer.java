package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilder.FieldBuilder;
import lombok.Data;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public abstract class FieldRenderer extends Renderer {
    PdfField helperField;

    public abstract void renderValue(PdfField field, int lineCounter) throws IOException;

    protected int renderLabel(PdfField field, PDType0Font font, int fontSize, Color colorLabel) throws IOException {
        float textWidth = getTextWidth(Collections.singletonList(field.getLabel()), font, fontSize, resource);
        int maxLineSize = getMaxLabelLineSize(field.getWidth(), fontSize);
        List<String> multiLineText = new ArrayList<String>() {{
            add(field.getLabel());
        }};
        int linesOnPage = 0;
        int x = field.getX() + padding, y = renderLinePosY(field, 1);

        if (textWidth > field.getWidth() - padding) {
            multiLineText = FieldBuilder.generateMultiLineText(Collections.singletonList(field.getLabel()), maxLineSize);
        }

        for (String line : multiLineText) {
            linesOnPage++;
            linesOnPage = renderPageBrake(field, linesOnPage, y);
            y = renderLinePosY(field, linesOnPage);
            pdfDrawer.writeString(font, fontSize, x, y, line, colorLabel);
        }
        pdfDrawer.checkOpenPages();
        return multiLineText.size();
    }

    protected int renderPageBrake(PdfField field, int linesOnPage, int y) throws IOException {
        if (y < marginBottom) {
            field.setHeight(renderHeight(field, linesOnPage));
            linesOnPage = 1;
            while (y < marginBottom) {
                pdfDrawer.newPage();
                field.setBottomY(renderBottomY(field));
                y = renderLinePosY(field, linesOnPage);
            }
        }
        return linesOnPage;
    }

    protected int renderPageBrake(PdfField field, int linesOnPage, int strokeLineCounter, int y) throws IOException {
        if (y < marginBottom) {
            if (resource.isTextFieldStroke()) {
                pdfDrawer.drawStroke(field.getX(), y, field.getBottomY(), field.getWidth(), strokeLineCounter, strokeWidth);
            }
            field.setHeight(renderHeight(field, linesOnPage));
            linesOnPage = 1;
            while (y < marginBottom) {
                pdfDrawer.newPage();
                field.setBottomY(renderBottomY(field));
                y = renderLinePosY(field, linesOnPage);
            }
        }
        return linesOnPage;
    }

    protected int renderLinePosY(PdfField field, int linesOnPage) {
        return field.getBottomY() + field.getHeight() - lineHeight * linesOnPage;
    }

    protected int renderBottomY(PdfField field) {
        return field.getBottomY() + pageHeight - marginTop - marginBottom - lineHeight;
    }

    protected int renderHeight(PdfField field, int linesOnPage) {
        return field.getHeight() - lineHeight * (linesOnPage - 1);
    }

    protected int getMaxValueLineSize(int fieldWidth) {
        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontValueSize);
    }
}
