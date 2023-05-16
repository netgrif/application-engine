 package com.netgrif.application.engine.pdf.generator.service.renderer;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

 @Component
 @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfPageNumberRenderer extends PdfFieldRenderer {

     public static final String PAGE_NUMBER_TYPE = "page_number";

    @Override
    public String[] getType() {
        return new String[]{PAGE_NUMBER_TYPE};
    }

    @Override
    public void renderValue() throws IOException {
        for (PDPage page : getPdfDrawer().getPageList()) {
            getPdfDrawer().getContentStream().close();
            getPdfDrawer().setContentStream(new PDPageContentStream(getPdfDrawer().getPdf(), page, PDPageContentStream.AppendMode.APPEND, true, true));
            String numberText = renderPageNumber(getPdfDrawer().getPageList().indexOf(page) + 1, getPdfDrawer().getPageList().size());
            getPdfDrawer().writeString(
                    getResource().getValueFont(), 
                    getResource().getFontValueSize(), 
                    getResource().getPageNumberPosition(), 
                    getResource().getMarginBottom() - 2 * getResource().getLineHeight(), 
                    numberText,
                    Color.decode(getResource().getColorString().toUpperCase()));
        }
    }

    private String renderPageNumber(int counter, int pageCount) {
        return counter + (!getResource().getPageNumberFormat().getFormat().equals("") ? getResource().getPageNumberFormat().getFormat() + pageCount : "");
    }

}
