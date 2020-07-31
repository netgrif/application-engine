package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfSelectionField;

public abstract class SelectionFieldBuilder extends FieldBuilder {

    public SelectionFieldBuilder(PdfResource resource) {
        super(resource);
    }

    protected void setFieldPositions(PdfSelectionField pdfField, int fontSize) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField));
        pdfField.setTopY(countTopPosY(pdfField));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField,resource));
        pdfField.setBottomY(countBottomPosY(pdfField,resource));
        pdfField.countMultiLineHeight(fontSize, resource);
    }
}
