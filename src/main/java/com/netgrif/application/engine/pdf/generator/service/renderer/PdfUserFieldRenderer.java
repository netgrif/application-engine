package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfDateField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfUserField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfUserFieldRenderer extends PdfFieldRenderer<PdfUserField> {

    @Override
    public String getType() {
        return DataType.USER.value();
    }

    @Override
    public void renderValue() throws IOException {
        PdfUserField clonedField = (PdfUserField) getField().getCopier().copyOf();
        String text = clonedField.getValue();
        int lineCounter = getLineCounter();
        int x = clonedField.getX() + getResource().getPadding(), y = renderLinePosY(clonedField, lineCounter);
        lineCounter++;
        lineCounter = renderPageBrake(clonedField, lineCounter, 0, y);
        y = renderLinePosY(clonedField, lineCounter);
        getPdfDrawer().writeString(getResource().getValueFont(), getResource().getFontValueSize(), x, y, text, Color.decode(getResource().getColorString().toUpperCase()));
        if (getResource().isTextFieldStroke()) {
            getPdfDrawer().drawStroke(clonedField.getX(), y, clonedField.getBottomY(), clonedField.getWidth(), 1, getResource().getStrokeWidth());
        }
        getPdfDrawer().checkOpenPages();
    }
}