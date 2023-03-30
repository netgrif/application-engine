package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfMultiChoiceField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfMultiChoiceMapField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfMultiChoiceMapFieldRenderer extends PdfFieldRenderer<PdfMultiChoiceMapField> {

    @Override
    public String getType() {
        return DataType.MULTICHOICE_MAP.value();
    }

    @Override
    public void renderValue() throws IOException {
        PdfMultiChoiceMapField clonedField = (PdfMultiChoiceMapField) getField().getCopier().copyOf();
        List<String> multiLineText = new ArrayList<>(clonedField.getSelectedValues());
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
}
