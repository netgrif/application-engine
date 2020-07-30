package com.netgrif.workflow.pdf.generator.service.renderer;

import com.netgrif.workflow.pdf.generator.config.types.PdfPageNumberFormat;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import lombok.Setter;

import java.io.IOException;

public class PageNumberRenderer extends Renderer {

    @Setter
    private PdfPageNumberFormat format;

    public void renderPageNumber(int counter, int pageCount) throws IOException {
        String numberText = generateNumberText(counter, pageCount);
        pdfDrawer.writeString(resource.getValueFont(), fontValueSize, marginLeft + (pageDrawableWidth / 2), marginBottom - 2 * lineHeight, numberText);
    }

    private String generateNumberText(int counter, int pageCount){
        return counter + (!format.getFormat().equals("") ? format.getFormat() + pageCount : "");
    }

    @Override
    public int renderLabel(PdfField field) throws IOException {return 0;}

    @Override
    public void setFieldParams(PdfField field) {}
}
