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

//    int marginLeft, marginBottom, marginTop;
//
//    int lineHeight, pageDrawableWidth, padding, pageHeight, baseX;
//    int fontValueSize, fontLabelSize, fontTitleSize;
//
//    float strokeWidth;
//
//    Color colorString, colorLabelString, colorDataGroupLabel;

    public PdfFieldRenderer() {
    }

    public void setField(PdfField<?> field) {
        this.field = (T) field;
    }

    public abstract String getType();

    public abstract void renderValue() throws IOException;


//    public void setupRenderer() {
//        this.marginLeft = resource.getMarginLeft();
//        this.marginBottom = resource.getMarginBottom();
//        this.marginTop = resource.getMarginTop();
//        this.lineHeight = resource.getLineHeight();
//        this.pageDrawableWidth = resource.getPageDrawableWidth();
//        this.padding = resource.getPadding();
//        this.colorString = Color.decode(resource.getColorString().toUpperCase());
//        this.colorDataGroupLabel = Color.decode(resource.getColorDataGroup().toUpperCase());
//        this.colorLabelString = Color.decode(resource.getColorLabelString().toUpperCase());
//        this.baseX = resource.getBaseX();
//        this.pageHeight = resource.getPageHeight();
//        this.fontValueSize = resource.getFontValueSize();
//        this.fontLabelSize = resource.getFontLabelSize();
//        this.fontTitleSize = resource.getFontTitleSize();
//        this.strokeWidth = resource.getStrokeWidth();
//    }

    public void renderLabel() throws IOException {
//        float textWidth = getTextWidth(field.getLabel(), resource.getLabelFont(), resource.getFontLabelSize(), resource);
//        int maxLineSize = PdfGeneratorUtils.getMaxLineSize(field.getWidth(), resource.getFontLabelSize());
        T clonedField = (T) field.getCopier().copyOf();
        if (clonedField.isLabelEmpty()) {
            return;
        }
        List<String> multiLineText = clonedField.getLabel();
        int linesOnPage = 0;
        int x = clonedField.getX() + resource.getPadding(), y = renderLinePosY(clonedField, 1);

//        if (textWidth > field.getWidth() - padding) {
//            multiLineText = PdfFieldBuilder.generateMultiLineText(field.getLabel(), maxLineSize);
//        }

        for (String line : multiLineText) {
            linesOnPage++;
            linesOnPage = renderPageBrake(clonedField, linesOnPage, y);
            //if (multiLineText.indexOf(line) == 0) {
            y = renderLinePosY(clonedField, linesOnPage);
            //}
            pdfDrawer.writeString(resource.getLabelFont(), resource.getFontLabelSize(), x, y, line, Color.decode(resource.getColorLabelString().toUpperCase()));
        }
        pdfDrawer.checkOpenPages();
        this.lineCounter = linesOnPage;
    }


//    protected int getMaxLabelLineSize(int fieldWidth, int fontSize) {
//        return (int) ((fieldWidth - resource.getPadding()) * resource.getSizeMultiplier() / fontSize);
//    }

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

//    protected int getMaxValueLineSize(int fieldWidth) {
//        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontValueSize);
//    }

//    private String removeUnsupportedChars(String input, PdfResource resource) {
//        String value = Jsoup.parse(input.replaceAll("\\s{1,}", " ")).text();
//        value = Normalizer.normalize(value, Normalizer.Form.NFC);
//        StringBuilder b = new StringBuilder();
//        for (int i = 0; i < value.length(); i++) {
//            if (isCharEncodable(value.charAt(i), resource.getValueFont())) {
//                b.append(value.charAt(i));
//            } else if (isCharEncodable(value.charAt(i), resource.getLabelFont())) {
//                b.append(value.charAt(i));
//            } else if (isCharEncodable(value.charAt(i), resource.getTitleFont())) {
//                b.append(value.charAt(i));
//            }
//        }
//        return b.toString();
//    }
//
//    private boolean isCharEncodable(char character, PDType0Font font) {
//        try {
//            font.encode(Character.toString(character));
//            return true;
//        } catch (IllegalArgumentException | IOException iae) {
//            return false;
//        }
//    }
}
