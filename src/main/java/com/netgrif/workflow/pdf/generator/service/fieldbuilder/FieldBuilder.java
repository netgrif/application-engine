package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.petrinet.domain.DataFieldLogic;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldLayout;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public abstract class FieldBuilder {
    protected PdfResource resource;

    @Getter
    protected int lastX, lastY;

    public FieldBuilder(PdfResource resource){
        this.resource = resource;
    }

    protected String getTranslatedLabel(String fieldStringId, PetriNet petriNet){
        return petriNet.getDataSet().get(fieldStringId).getName().getTranslation(resource.getTextLocale());
    }

    protected void setFieldParams(DataGroup dg, DataFieldLogic field, PdfField pdfField) {
        pdfField.setLayoutX(countFieldLayoutX(dg, field));
        pdfField.setLayoutY(countFieldLayoutY(dg, field));
        pdfField.setWidth(countFieldWidth(dg, field));
        pdfField.setHeight(countFieldHeight(field));
    }

    protected void setFieldPositions(PdfField pdfField, int fontSize) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField, resource));
        pdfField.setTopY(countTopPosY(pdfField, resource));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField,resource));
        pdfField.setBottomY(countBottomPosY(pdfField,resource));
        pdfField.countMultiLineHeight(fontSize, resource);
    }

    private int countFieldLayoutX(DataGroup dataGroup, DataFieldLogic field) {
        int x = 0;
        if (checkCol(field.getLayout())) {
            x = field.getLayout().getX();
            lastX = x;
        } else if (dataGroup.getStretch() == null || !dataGroup.getStretch()) {
            lastX = (lastX == 0 ? 2 : 0);
            x = lastX;
        }
        return x;
    }

    private int countFieldLayoutY(DataGroup dataGroup, DataFieldLogic field) {
        int y;
        if (checkRow(field.getLayout())) {
            y = field.getLayout().getY();
            lastY = y;
        } else if (dataGroup.getStretch() != null && dataGroup.getStretch()) {
            y = ++lastY;
        } else {
            lastY = (lastX == 0 ? ++lastY : lastY);
            y = lastY;
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
        int lineLen = 1;

        for (String value : values) {
            tokenizer = new StringTokenizer(value, " ");
            output = new StringBuilder(value.length());
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();

                if (lineLen + word.length() > maxLineLength) {
                    output.append("\n");
                    lineLen = 0;
                }
                output.append(word + " ");
                lineLen += word.length() + 1;
            }
            result.addAll(Arrays.asList(output.toString().split("\n")));
        }
        return result;
    }

    private int countFieldWidth(DataGroup dataGroup, DataFieldLogic field) {
        if (checkCol(field.getLayout())) {
            return field.getLayout().getCols() * resource.getFormGridColWidth() - resource.getPadding();
        } else {
            return (dataGroup.getStretch() != null && dataGroup.getStretch() ?
                    (resource.getFormGridColWidth() * resource.getFormGridCols())
                    : (resource.getFormGridColWidth() * resource.getFormGridCols() / 2)) - resource.getPadding();
        }
    }

    private int countFieldHeight(DataFieldLogic field) {
        if (checkRow(field.getLayout())) {
            return field.getLayout().getRows() * resource.getFormGridRowHeight() - resource.getPadding();
        } else {
            return resource.getFormGridRowHeight() - resource.getPadding();
        }
    }

    private boolean checkRow(FieldLayout layout){
        return layout != null && layout.getRows() != null;
    }

    private boolean checkCol(FieldLayout layout){
        return layout != null && layout.getCols() != null;
    }
}
