package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfBooleanField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfBooleanFieldRenderer extends PdfFieldRenderer<Boolean, PdfBooleanField> {

    @Override
    public String getType() {
        return DataType.BOOLEAN.value();
    }

    @Override
    public void renderValue() throws IOException {
        PdfBooleanField clonedField = (PdfBooleanField) getField().getCopier().copyOf();
        clonedField.setFormat(getField().getFormat());
        int lineCounter = getLineCounter();
        int x = clonedField.getX() + getResource().getPadding(), y = renderLinePosY(clonedField, lineCounter);
        lineCounter++;
        lineCounter = renderPageBrake(clonedField, lineCounter, y);
        y = renderLinePosY(clonedField, lineCounter);
        if (getResource().isBooleanFieldStroke()) {
            getPdfDrawer().drawStroke(clonedField.getX(), y, clonedField.getBottomY(), clonedField.getWidth(), 1, getResource().getStrokeWidth());
        }

        int index = 0;
        Map<Boolean, String> booleanTexts = clonedField.getFormat().getValue();
        for (Map.Entry<Boolean, String> booleanText : booleanTexts.entrySet()) {
            x += index * getResource().getPadding() * 9;
            getPdfDrawer().drawBooleanBox(clonedField.getValue(), booleanText, x, y);
            getPdfDrawer().writeString(getResource().getValueFont(), getResource().getFontValueSize(), x + getResource().getFontLabelSize() + getResource().getPadding(), y, booleanText.getValue(), Color.decode(getResource().getColorString().toUpperCase()));
            index++;
        }
        getPdfDrawer().checkOpenPages();
    }

//    private void renderValue(PdfField field, int lineCounter, float strokeWidth) throws IOException {
//        int x = field.getX() + padding, y = renderLinePosY(field, lineCounter);
//        lineCounter++;
//        lineCounter = renderPageBrake(field, lineCounter, y);
//        y = renderLinePosY(field, lineCounter);
//        if (resource.isBooleanFieldStroke()) {
//            pdfDrawer.drawStroke(field.getX(), y, field.getBottomY(), field.getWidth(), 1, strokeWidth);
//        }
//
//        List<String> booleanValues = booleanFormat.getValue();
//        for (String value : booleanValues) {
//            x += booleanValues.indexOf(value) * (padding * 9);
//            pdfDrawer.drawBooleanBox(field.getValue(), value, x, y);
//            pdfDrawer.writeString(resource.getValueFont(), resource.getFontValueSize(), x + fontLabelSize + padding, y, value, colorString);
//        }
//        pdfDrawer.checkOpenPages();
//    }
}
