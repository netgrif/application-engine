package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfSelectionField;
import com.netgrif.workflow.petrinet.domain.I18nString;

import java.util.List;
import java.util.Set;

public abstract class SelectionFieldBuilder extends FieldBuilder {

    public SelectionFieldBuilder(PdfResource resource) {
        super(resource);
    }

    protected abstract List<String> getTranslatedSet(Set<I18nString> choices);

    protected abstract String getTranslatedString(Set<I18nString> choices, String value);

    protected void setFieldPositions(PdfSelectionField pdfField, int fontSize) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField));
        pdfField.setTopY(countTopPosY(pdfField));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField,resource));
        pdfField.setBottomY(countBottomPosY(pdfField,resource));
        pdfField.countMultiLineHeight(fontSize, resource);
    }

}
