package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.domain.fields.PdfDataGroupField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfDataGroupFieldRenderer extends PdfFieldRenderer<PdfDataGroupField> {


    @Override
    public String[] getType() {
        return new String[]{PdfDataGroupField.DATA_GROUP_TYPE};
    }

    @Override
    public void renderValue() throws IOException {
        PdfDataGroupField clonedField = (PdfDataGroupField) getField().getCopier().copyOf();
        if (clonedField.isValueEmpty()) {
            return;
        }
        List<String> multiLineText = clonedField.getValue();
        int lineCounter = 0, x = clonedField.getX() + getResource().getPadding(), y = renderLinePosY(clonedField, 1);

        for (String line : multiLineText) {
            lineCounter++;
            lineCounter = renderPageBrake(clonedField, lineCounter, y);
            y = renderLinePosY(clonedField, lineCounter);
            getPdfDrawer().writeString(getResource().getLabelFont(), getResource().getFontGroupSize(), x, y, line, Color.decode(getResource().getColorDataGroup().toUpperCase()));
        }
        getPdfDrawer().checkOpenPages();
    }

}
