package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTextField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfTextFieldRenderer extends PdfFieldRenderer<PdfTextField> {

    public PdfTextFieldRenderer() {
        super();
    }

    @Override
    public String[] getType() {
        return new String[]{
                DataType.TEXT.value(),
                DataType.TEXT.value() + "_" + "password",
                DataType.TEXT.value() + "_" + "textarea",
                DataType.TEXT.value() + "_" + "area",
                DataType.TEXT.value() + "_" + "richtextarea",
                DataType.TEXT.value() + "_" + "editor",
                DataType.TEXT.value() + "_" + "htmltextarea",
                DataType.TEXT.value() + "_" + "htmlEditor"
        };
    }

    @Override
    public void renderValue() throws IOException {
        PdfTextField clonedField = (PdfTextField) getField().getCopier().copyOf();
        List<String> multiLineText = clonedField.getValue();
        int lineCounter = getLineCounter();
        int x = clonedField.getX() + getResource().getPadding(), y = renderLinePosY(clonedField, lineCounter);
        int strokeLineCounter = 0;

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
}
