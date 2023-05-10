package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfI18nDividerField;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class PdfI18nDividerFieldRenderer extends PdfFieldRenderer<PdfI18nDividerField> {

    @Override
    public String[] getType() {
        return new String[]{DataType.I_18_N.value()};
    }

    @Override
    public void renderValue() throws IOException {
        PdfI18nDividerField clonedField = (PdfI18nDividerField) getField().getCopier().copyOf();
        List<String> multiLineText = clonedField.getValue();
        int linesOnPage = 0;
        int x = clonedField.getX() + getResource().getPadding(), y = renderLinePosY(clonedField, 1);

        for (String line : multiLineText) {
            linesOnPage++;
            linesOnPage = renderPageBrake(clonedField, linesOnPage, y);
            y = renderLinePosY(clonedField, linesOnPage);
            getPdfDrawer().writeString(getResource().getLabelFont(), getResource().getFontGroupSize(), x, y, line, Color.decode(getResource().getColorDataGroup().toUpperCase()));
        }
        getPdfDrawer().checkOpenPages();
    }

//    public void setFieldParams(PdfField field) {
//        helperField = new PdfTextField(field.getFieldId(), field.getLabel(), field.getValue(), field.getType(),
//                resource.getBaseX() + field.getX(), resource.getBaseY() - field.getBottomY(), field.getWidth(), field.getHeight(), resource);
//    }
//
//    @Override
//    public int renderLabel(PdfField field) throws IOException {
//        return 0;
//    }
//
//    @Override
//    public void renderValue(PdfField field, int lineCounter) throws IOException {
//        setFieldParams(field);
//        renderValue(helperField, resource.getLabelFont(), resource.getFontGroupSize(), colorDataGroupLabel);
//    }

//    private void renderValue(PdfField field, PDType0Font font, int fontSize, Color colorLabel) throws IOException {
//        float textWidth = getTextWidth(Collections.singletonList(field.getLabel()), font, fontSize, resource);
//        int maxLineSize = getMaxLabelLineSize(field.getWidth(), fontSize);
//        List<String> multiLineText = new ArrayList<>(field.getValue());
//        int linesOnPage = 0;
//        int x = field.getX() + padding, y = renderLinePosY(field, 1);
//
//        if (textWidth > field.getWidth() - padding) {
//            multiLineText = PdfFieldBuilder.generateMultiLineText(field.getValue(), maxLineSize);
//        }
//
//        for (String line : multiLineText) {
//            linesOnPage++;
//            linesOnPage = renderPageBrake(field, linesOnPage, y);
//            y = renderLinePosY(field, linesOnPage);
//            pdfDrawer.writeString(font, fontSize, x, y, line, colorLabel);
//        }
//        pdfDrawer.checkOpenPages();
  //  }
}
