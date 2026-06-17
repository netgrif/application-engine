package com.netgrif.application.engine.pdf.generator.service.renderer;

import com.netgrif.application.engine.pdf.generator.config.types.PdfPageNumberFormat;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import lombok.Setter;

import java.io.IOException;

public class PageNumberRenderer extends Renderer {

    @Setter
    private PdfPageNumberFormat format;

    public void renderPageNumber(int counter, int pageCount) throws IOException {
        String numberText = generateNumberText(counter, pageCount);
        pdfDrawer.writeString(resource.getValueFont(), fontValueSize, resource.getPageNumberPosition(), marginBottom - 2 * lineHeight, numberText, colorString);
    }

    private String generateNumberText(int counter, int pageCount) {
        return counter + (!format.getFormat().equals("") ? format.getFormat() + pageCount : "");
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {
        return 0;
    }

}
