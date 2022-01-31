package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfSelectionField;

public abstract class SelectionFieldBuilder extends FieldBuilder {

    public SelectionFieldBuilder(PdfResource resource) {
        super(resource);
    }

    protected void setFieldPositions(PdfSelectionField pdfField, int fontSize) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField, resource));
        pdfField.setTopY(countTopPosY(pdfField, resource));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField, resource));
        pdfField.setBottomY(countBottomPosY(pdfField, resource));
        pdfField.countMultiLineHeight(fontSize, resource);
    }
}
