package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfTitleField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilder.FieldBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TitleRenderer extends Renderer {

    private int fieldY, fieldWidth;
    private String label;

    public void setFieldParams(PdfTitleField field) {
        fieldY = field.getBottomY();
        fieldWidth = field.getWidth();
        label = field.getLabel();
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {
        setFieldParams((PdfTitleField) field);
        renderTitle(label, fieldY, fieldWidth);
        return 0;
    }

    private void renderTitle(String title, int fieldY, int fieldWidth) throws IOException {
        float textWidth = getTextWidth(Collections.singletonList(title), resource.getTitleFont(), fontTitleSize, resource);
        List<String> multiLineText = new ArrayList<>();
        int lineCounter = 0, x = (int) (baseX + ((pageDrawableWidth - textWidth) / 2)), y;
        int maxLineSize = getMaxLabelLineSize(fieldWidth, fontTitleSize);

        multiLineText.add(title);

        if (textWidth > fieldWidth - 2 * padding) {
            x = baseX;
            multiLineText = FieldBuilder.generateMultiLineText(Collections.singletonList(title), maxLineSize);
        }

        for (String line : multiLineText) {
            y = pageHeight - resource.getMarginTitle() - fieldY - lineHeight * lineCounter;
            pdfDrawer.writeString(resource.getTitleFont(), fontTitleSize, x, y, line, colorString);
            lineCounter++;
        }
    }

}
