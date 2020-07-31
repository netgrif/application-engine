package com.netgrif.workflow.pdf.generator.service.renderer;

import com.netgrif.workflow.pdf.generator.domain.PdfSelectionField;
import com.netgrif.workflow.pdf.generator.service.fieldbuilder.FieldBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SelectionFieldRenderer<T> extends FieldRenderer<T>{


    public void renderValue(PdfSelectionField field, int lineCounter) throws IOException {
        int maxLineSize = getMaxValueLineSize(field.getWidth() - 3 * padding);
        List<String> multiLineText;
        int x = field.getX() + 4 * padding, y = renderLinePosY(field, lineCounter);

        for (String choice : field.getChoices()) {
            boolean buttonDrawn = false;
            float textWidth = getTextWidth(Collections.singletonList(choice), resource.getValueFont(), fontValueSize);
            multiLineText = new ArrayList<String>() {{
                add(choice);
            }};

            if (textWidth > field.getWidth() - 4 * padding) {
                multiLineText = FieldBuilder.generateMultiLineText(Collections.singletonList(choice), maxLineSize);
            }

            for (String line : multiLineText) {
                lineCounter++;
                lineCounter = renderPageBrake(field, lineCounter, y);
                y = renderLinePosY(field, lineCounter);
                pdfDrawer.writeString(resource.getValueFont(), fontValueSize, x, y, line);
                if (!buttonDrawn) {
                    buttonDrawn = pdfDrawer.drawSelectionButton(field.getValues(), choice, field.getX() + padding, y, field.getType());
                }
            }
        }
        pdfDrawer.checkOpenPages();
    }
}
