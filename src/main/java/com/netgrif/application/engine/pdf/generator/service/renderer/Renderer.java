package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDrawer;
import lombok.Data;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;

@Data
public abstract class Renderer {

    protected IPdfDrawer pdfDrawer;

    protected PdfResource resource;

    int marginLeft, marginBottom, marginTop;

    int lineHeight, pageDrawableWidth, padding, pageHeight, baseX;
    int fontValueSize, fontLabelSize, fontTitleSize;

    float strokeWidth;

    Color colorString, colorLabelString, colorDataGroupLabel;

    public abstract int renderLabel(PdfField field) throws IOException;

    public void setupRenderer(IPdfDrawer pdfDrawer, PdfResource resource) {
        this.pdfDrawer = pdfDrawer;
        this.resource = resource;
        this.marginLeft = resource.getMarginLeft();
        this.marginBottom = resource.getMarginBottom();
        this.marginTop = resource.getMarginTop();
        this.lineHeight = resource.getLineHeight();
        this.pageDrawableWidth = resource.getPageDrawableWidth();
        this.padding = resource.getPadding();
        this.colorString = Color.decode(resource.getColorString().toUpperCase());
        this.colorDataGroupLabel = Color.decode(resource.getColorDataGroup().toUpperCase());
        this.colorLabelString = Color.decode(resource.getColorLabelString().toUpperCase());
        this.baseX = resource.getBaseX();
        this.pageHeight = resource.getPageHeight();
        this.fontValueSize = resource.getFontValueSize();
        this.fontLabelSize = resource.getFontLabelSize();
        this.fontTitleSize = resource.getFontTitleSize();
        this.strokeWidth = resource.getStrokeWidth();
    }


    protected static int getTextWidth(List<String> values, PDType0Font font, int fontSize, PdfResource resource) throws IOException {
        int result = 0;
        for (String value : values) {
            String formattedValue = removeUnsupportedChars(value, resource);
            if (result < font.getStringWidth(formattedValue) / 1000 * fontSize)
                result = (int) (font.getStringWidth(formattedValue) / 1000 * fontSize);
        }
        return result;
    }

    protected int getMaxLabelLineSize(int fieldWidth, int fontSize) {
        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontSize);
    }

    public static String removeUnsupportedChars(String input, PdfResource resource) {
        String value = Jsoup.parse(input.replaceAll("\\s{1,}", " ")).text();
        value = Normalizer.normalize(value, Normalizer.Form.NFC);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (isCharEncodable(value.charAt(i), resource.getValueFont())) {
                b.append(value.charAt(i));
            } else if (isCharEncodable(value.charAt(i), resource.getLabelFont())) {
                b.append(value.charAt(i));
            } else if (isCharEncodable(value.charAt(i), resource.getTitleFont())) {
                b.append(value.charAt(i));
            }
        }
        return b.toString();
    }

    public static boolean isCharEncodable(char character, PDType0Font font) {
        try {
            font.encode(Character.toString(character));
            return true;
        } catch (IllegalArgumentException | IOException iae) {
            return false;
        }
    }
}
