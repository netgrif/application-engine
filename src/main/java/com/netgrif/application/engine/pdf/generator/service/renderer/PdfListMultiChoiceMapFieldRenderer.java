package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfMultiChoiceMapField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfSelectionField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfListMultiChoiceMapFieldRenderer extends PdfMultiChoiceMapFieldRenderer {

    @Override
    public String[] getType() {
        return new String[]{DataType.MULTICHOICE_MAP.value() + "_" + PdfSelectionField.LIST_COMPONENT_NAME};
    }

    @Override
    public void renderValue() throws IOException {
        PdfMultiChoiceMapField clonedField = (PdfMultiChoiceMapField) getField().getCopier().copyOf();
        int lineCounter = getLineCounter();
        int x = clonedField.getX() + 4 * getResource().getPadding(), y = renderLinePosY(clonedField, lineCounter);

        for (Map.Entry<String, List<String>> choice : clonedField.getValue().entrySet()) {
            boolean buttonDrawn = false;
            for (String line : choice.getValue()) {
                lineCounter++;
                lineCounter = renderPageBrake(clonedField, lineCounter, y);
                y = renderLinePosY(clonedField, lineCounter);
                getPdfDrawer().writeString(getResource().getValueFont(), getResource().getFontValueSize(), x, y, line, Color.decode(getResource().getColorString().toUpperCase()));
                if (!buttonDrawn) {
                    buttonDrawn = getPdfDrawer().drawSelectionButton(clonedField.getSelectedValues(), choice.getKey(), clonedField.getX() + getResource().getPadding(), y, clonedField.getType());
                }
            }
        }
        getPdfDrawer().checkOpenPages();
    }
}
