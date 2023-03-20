//package com.netgrif.application.engine.pdf.generator.service.renderer;
//
//import com.netgrif.application.engine.pdf.generator.domain.PdfField;
//import com.netgrif.application.engine.pdf.generator.domain.PdfMultiChoiceField;
//import com.netgrif.application.engine.pdf.generator.domain.PdfSelectionField;
//
//import java.io.IOException;
//
//public class MultiChoiceRenderer extends SelectionFieldRenderer {
//
//    public void setFieldParams(PdfMultiChoiceField field) {
//        helperField = new PdfMultiChoiceField(field.getFieldId(), field.getLabel(), field.getValue(), field.getChoices(), field.getType(), resource.getBaseX() + field.getX(),
//                resource.getBaseY() - field.getBottomY(), field.getWidth(), field.getHeight(), resource);
//    }
//
//    @Override
//    public int renderLabel(PdfField field) throws IOException {
//        setFieldParams((PdfMultiChoiceField) field);
//        return renderLabel(helperField, resource.getLabelFont(), fontLabelSize, colorLabelString);
//    }
//
//    public void renderValue(PdfField field, int lineCounter) throws IOException {
//        setFieldParams((PdfMultiChoiceField) field);
//        renderValue((PdfSelectionField) helperField, lineCounter);
//    }
//}
