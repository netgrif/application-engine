package com.netgrif.workflow.pdf.generator.service.renderer;

import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.DataConverter;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SelectionFieldRenderer<T> extends FieldRenderer<T>{


    public void renderValue(PdfField field, int lineCounter) throws IOException {
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
                multiLineText = DataConverter.generateMultiLineText(Collections.singletonList(choice), maxLineSize);
            }

            for (String line : multiLineText) {
                lineCounter++;
                lineCounter = renderPageBrake(field, lineCounter, y);
                y = renderLinePosY(field, lineCounter);
                pdfDrawer.writeString(resource.getValueFont(), fontValueSize, x, y, line);
                if (!buttonDrawn) {
                    buttonDrawn = pdfDrawer.drawSelectionButtons(field.getValues(), choice, field.getX() + padding, y, field.getType());
                }
            }
        }
        pdfDrawer.checkOpenPages();
    }

    protected void drawValue(List<String> choices, List<String> values, int fieldX, int fieldY, int fieldWidth, int fieldHeight, FieldType type, int lineCounter) throws IOException {
        int maxLineSize = getMaxValueLineSize(fieldWidth - 3 * padding);
        List<String> multiLineText;
        int x = fieldX + 4 * padding, y = fieldY + fieldHeight - lineHeight * lineCounter;

        for (String choice : choices) {
            boolean buttonDrawn = false;
            float textWidth = getTextWidth(Collections.singletonList(choice), resource.getValueFont(), fontValueSize);
            multiLineText = new ArrayList<String>() {{
                add(choice);
            }};

            if (textWidth > fieldWidth - 4 * padding) {
                multiLineText = DataConverter.generateMultiLineText(Collections.singletonList(choice), maxLineSize);
            }

            for (String line : multiLineText) {
                lineCounter++;
                if (y < marginBottom) {
                    fieldHeight -= lineHeight * (lineCounter - 1);
                    lineCounter = 1;
                    while (y < marginBottom) {
                        pdfDrawer.newPage();
                        fieldY = fieldY + pageHeight - marginTop - marginBottom - lineHeight;
                        y = fieldY + fieldHeight - lineHeight;
                    }
                }
                y = fieldY + fieldHeight - lineHeight * lineCounter;
                pdfDrawer.writeString(resource.getValueFont(), fontValueSize, x, y, line);
                if (!buttonDrawn) {
                    buttonDrawn = pdfDrawer.drawSelectionButtons(values, choice, fieldX + padding, y, type);
                }
            }
        }
        pdfDrawer.checkOpenPages();
    }
}
