package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDrawer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Data
@EqualsAndHashCode
public abstract class PdfFieldRenderer<T extends PdfField<?>> {

    private T field;

    private IPdfDrawer pdfDrawer;

    private PdfResource resource;

    private int lineCounter;

    public PdfFieldRenderer() {
    }

    public void setField(PdfField<?> field) {
        this.field = (T) field;
    }

    public abstract String getType();

    public abstract void renderValue() throws IOException;

    public void renderLabel() throws IOException {
        T clonedField = (T) field.getCopier().copyOf();
        if (clonedField.isLabelEmpty()) {
            return;
        }
        List<String> multiLineText = clonedField.getLabel();
        int linesOnPage = 0;
        int x = clonedField.getX() + resource.getPadding(), y = renderLinePosY(clonedField, 1);

        for (String line : multiLineText) {
            linesOnPage++;
            linesOnPage = renderPageBrake(clonedField, linesOnPage, y);
            y = renderLinePosY(clonedField, linesOnPage);
            pdfDrawer.writeString(resource.getLabelFont(), resource.getFontLabelSize(), x, y, line, Color.decode(resource.getColorLabelString().toUpperCase()));
        }
        pdfDrawer.checkOpenPages();
        this.lineCounter = linesOnPage;
    }

    protected int renderPageBrake(PdfField<?> field, int linesOnPage, int y) throws IOException {
        if (y < resource.getMarginBottom()) {
            field.setHeight(renderHeight(field, linesOnPage));
            linesOnPage = 1;
            while (y < resource.getMarginBottom()) {
                pdfDrawer.newPage();
                field.setBottomY(renderBottomY(field));
                y = renderLinePosY(field, linesOnPage);
            }
        }
        return linesOnPage;
    }

    protected int renderPageBrake(PdfField<?> field, int linesOnPage, int strokeLineCounter, int y) throws IOException {
        if (y < resource.getMarginBottom()) {
            if (resource.isTextFieldStroke()) {
                pdfDrawer.drawStroke(field.getX(), y, field.getBottomY(), field.getWidth(), strokeLineCounter, resource.getStrokeWidth());
            }
            field.setHeight(renderHeight(field, linesOnPage));
            linesOnPage = 1;
            while (y < resource.getMarginBottom()) {
                pdfDrawer.newPage();
                field.setBottomY(renderBottomY(field));
                y = renderLinePosY(field, linesOnPage);
            }
        }
        return linesOnPage;
    }

    protected int renderLinePosY(PdfField<?> field, int linesOnPage) {
        return field.getBottomY() + field.getHeight() - resource.getLineHeight() * linesOnPage;
    }

    protected int renderBottomY(PdfField<?> field) {
        return field.getBottomY() + resource.getPageHeight() - resource.getMarginTop() - resource.getMarginBottom() - resource.getLineHeight();
    }

    protected int renderHeight(PdfField<?> field, int linesOnPage) {
        return field.getHeight() - resource.getLineHeight() * (linesOnPage - 1);
    }
}
