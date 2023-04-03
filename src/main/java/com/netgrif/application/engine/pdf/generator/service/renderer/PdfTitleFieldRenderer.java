package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTextField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTitleField;
import com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfTitleFieldRenderer extends PdfFieldRenderer<PdfTitleField> {

    @Override
    public void renderValue() throws IOException {
        PdfTitleField clonedField = (PdfTitleField) getField().getCopier().copyOf();;
        if (clonedField.isValueEmpty()) {
            return;
        }
        List<String> multiLineText = clonedField.getValue();
        float textWidth = PdfGeneratorUtils.getTextWidth(multiLineText, getResource().getTitleFont(), getResource().getFontTitleSize(), getResource());
        int lineCounter = 1, x = (int) (getResource().getBaseX() + ((getResource().getPageDrawableWidth() - textWidth) / 2)), y;
        for (String line : multiLineText) {
            y = getResource().getPageHeight() - getResource().getMarginTitle() - getResource().getTitleLineHeight() * lineCounter;
            getPdfDrawer().writeString(getResource().getTitleFont(), getResource().getFontTitleSize(), x, y, line, Color.decode(getResource().getColorString().toUpperCase()));
            lineCounter++;
        }
    }

    @Override
    public String getType() {
        return PdfTitleField.TITLE_TYPE;
    }
}
