package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTextField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfTextFieldRenderer extends PdfFieldRenderer<List<String>, PdfTextField> {

    public PdfTextFieldRenderer() {
        super();
    }

    @Override
    public String getType() {
        return DataType.TEXT.value();
    }

    @Override
    public void renderValue() throws IOException {
//        float textWidth = getTextWidth(getField().getValue(), getResource().getValueFont(), getResource().getFontValueSize(), getResource());
//        int maxLineSize = getMaxLineSize(
//                getField().getWidth() - 3 * getResource().getPadding(),
//                getResource().getFontValueSize(),
//                getResource().getPadding(),
//                getResource().getSizeMultiplier()
//        );
        PdfTextField clonedField = (PdfTextField) getField().getCopier().copyOf();
        List<String> multiLineText = clonedField.getValue();
        int lineCounter = getLineCounter();
        int x = clonedField.getX() + getResource().getPadding(), y = renderLinePosY(clonedField, lineCounter);
        int strokeLineCounter = 0;

//        if (textWidth > getField().getWidth() - 3 * getResource().getPadding()) {
//            multiLineText = generateMultiLineText(getField().getValue(), maxLineSize);
//        }

        for (String line : multiLineText) {
            lineCounter++;
            lineCounter = renderPageBrake(clonedField, lineCounter, strokeLineCounter, y);
            strokeLineCounter = lineCounter == 1 ? 0 : strokeLineCounter;
            y = renderLinePosY(clonedField, lineCounter);
            strokeLineCounter++;
            getPdfDrawer().writeString(getResource().getValueFont(), getResource().getFontValueSize(), x, y, line, Color.decode(getResource().getColorString().toUpperCase()));
        }
        if (getResource().isTextFieldStroke()) {
            getPdfDrawer().drawStroke(clonedField.getX(), y, clonedField.getBottomY(), clonedField.getWidth(), strokeLineCounter, getResource().getStrokeWidth());
        }
        getPdfDrawer().checkOpenPages();
    }


//    public void setFieldParams(PdfTextField field) {
//        setField(field);
//    }

//    @Override
//    public int renderLabel(PdfTextField field) throws IOException {
//        setField(field);
//        return renderLabel();
//    }


//    private void renderValue(PdfTextField field, int lineCounter, float strokeWidth) throws IOException {
//        float textWidth = getTextWidth(field.getValue(), resource.getValueFont(), fontValueSize, resource);
//        int maxLineSize = getMaxValueLineSize(field.getWidth() - 3 * padding);
//        List<String> multiLineText = field.getValue();
//        int x = field.getX() + padding, y = renderLinePosY(field, lineCounter);
//        int strokeLineCounter = 0;
//
//        if (textWidth > field.getWidth() - 3 * padding) {
//            multiLineText = PdfFieldBuilder.generateMultiLineText(field.getValue(), maxLineSize);
//        }
//
//        for (String line : multiLineText) {
//            lineCounter++;
//            lineCounter = renderPageBrake(field, lineCounter, strokeLineCounter, y);
//            strokeLineCounter = lineCounter == 1 ? 0 : strokeLineCounter;
//            y = renderLinePosY(field, lineCounter);
//            strokeLineCounter++;
//            pdfDrawer.writeString(resource.getValueFont(), fontValueSize, x, y, line, colorString);
//        }
//        if (resource.isTextFieldStroke()) {
//            pdfDrawer.drawStroke(field.getX(), y, field.getBottomY(), field.getWidth(), strokeLineCounter, strokeWidth);
//        }
//        pdfDrawer.checkOpenPages();
//    }

}
