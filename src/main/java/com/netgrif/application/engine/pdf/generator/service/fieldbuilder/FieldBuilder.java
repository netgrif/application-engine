package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedField;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public abstract class FieldBuilder {
    protected PdfResource resource;

    @Getter
    protected int lastX, lastY;

    public FieldBuilder(PdfResource resource) {
        this.resource = resource;
    }

    protected void setFieldParams(DataGroup dg, LocalisedField field, PdfField pdfField) {
        pdfField.setLayoutX(countFieldLayoutX(dg, field));
        pdfField.setLayoutY(countFieldLayoutY(dg, field));
        pdfField.setWidth(countFieldWidth(dg, field));
        pdfField.setHeight(countFieldHeight());
    }

    protected void setFieldPositions(PdfField pdfField, int fontSize) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField, resource));
        pdfField.setTopY(countTopPosY(pdfField, resource));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField, resource));
        pdfField.setBottomY(countBottomPosY(pdfField, resource));
        pdfField.countMultiLineHeight(fontSize, resource);
    }

    private int countFieldLayoutX(DataGroup dataGroup, LocalisedField field) {
        int x = 0;
        if (isDgFlow(dataGroup)) {
            x = lastX < resource.getFormGridCols() ? lastX : 0;
            lastX = x + 1;
        } else if (field.getLayout() != null && !isStretch(dataGroup)) {
            x = field.getLayout().getX();
            lastX = x;
        } else if (dataGroup.getStretch() == null || !dataGroup.getStretch()) {
            lastX = (lastX == 0 ? 2 : 0);
            x = lastX;
        }
        return x;
    }

    private int countFieldLayoutY(DataGroup dataGroup, LocalisedField field) {
        int y;
        if (isDgFlow(dataGroup)) {
            if (resource.getRowGridFree() - 1 > 0) {
                resource.setRowGridFree(resource.getRowGridFree() - 1);
                y = lastY;
            } else {
                y = lastY++;
            }
        } else if (checkFullRow(dataGroup, field)) {
            y = ++lastY;
            resolveRowGridFree(dataGroup, field.getLayout());
        } else {
            if (lastX == 0) {
                y = ++lastY;
                resolveRowGridFree(dataGroup, field.getLayout());
            } else {
                y = lastY;
                resource.setRowGridFree(!checkCol(field.getLayout()) ? 2 : resource.getRowGridFree() - field.getLayout().getCols());
            }
        }
        return y;
    }

    public int countPosX(PdfField field) {
        return (field.getLayoutX() * resource.getFormGridColWidth() + resource.getPadding());
    }

    public static int countTopPosY(PdfField field, PdfResource resource) {
        return (field.getLayoutY() * resource.getFormGridRowHeight()) + resource.getPadding();
    }

    public static int countBottomPosY(PdfField field, PdfResource resource) {
        return (field.getLayoutY() * resource.getFormGridRowHeight()) + field.getHeight() + resource.getPadding();
    }

    public static List<String> generateMultiLineText(List<String> values, float maxLineLength) {
        StringTokenizer tokenizer;
        StringBuilder output;
        List<String> result = new ArrayList<>();
        int lineLen = 0;

        for (String value : values) {
            tokenizer = new StringTokenizer(value.trim(), " ");
            output = new StringBuilder(value.length());
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();

                if (lineLen + word.length() > maxLineLength) {
                    output.append("\n");
                    lineLen = 0;
                }
                output.append(word).append(" ");
                lineLen += word.length() + 1;
            }
            lineLen = 0;
            result.addAll(Arrays.asList(output.toString().split("\n")));
        }
        return result;
    }

    private int countFieldWidth(DataGroup dataGroup, LocalisedField field) {
        if (isDgFlow(dataGroup)) {
            return resource.getFormGridColWidth() - resource.getPadding();
        } else if (checkCol(field.getLayout()) && !isStretch(dataGroup)) {
            return field.getLayout().getCols() * resource.getFormGridColWidth() - resource.getPadding();
        } else {
            return (isStretch(dataGroup) ?
                    (resource.getFormGridColWidth() * resource.getFormGridCols())
                    : (resource.getFormGridColWidth() * resource.getFormGridCols() / 2)) - resource.getPadding();
        }
    }

    private int countFieldHeight() {
        return resource.getFormGridRowHeight() - resource.getPadding();
    }

    private boolean checkFullRow(DataGroup dataGroup, LocalisedField field) {
        return (isStretch(dataGroup)) ||
                (checkCol(field.getLayout()) && resource.getRowGridFree() < field.getLayout().getCols());
    }

    private boolean checkCol(FieldLayout layout) {
        return layout != null && layout.getCols() != null;
    }

    private boolean isStretch(DataGroup dataGroup) {
        return dataGroup.getStretch() != null && dataGroup.getStretch();
    }

    private boolean isDgFlow(DataGroup dataGroup) {
        return dataGroup.getLayout() != null && dataGroup.getLayout().getType() != null && dataGroup.getLayout().getType().equals("flow");
    }

    private void resolveRowGridFree(DataGroup dataGroup, FieldLayout layout) {
        if (checkCol(layout)) {
            resource.setRowGridFree(resource.getFormGridCols() - layout.getCols());
        } else {
            if (isStretch(dataGroup))
                resource.setRowGridFree(0);
            else {
                resource.setRowGridFree(resource.getFormGridCols() - 2);
            }
        }
    }
}
