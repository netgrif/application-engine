package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfSelectionField;
import com.netgrif.workflow.petrinet.domain.I18nString;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SelectionFieldBuilder extends FieldBuilder {

    public SelectionFieldBuilder(PdfResource resource) {
        super(resource);
    }

    protected void setFieldPositions(PdfSelectionField pdfField, int fontSize) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField, resource));
        pdfField.setTopY(countTopPosY(pdfField, resource));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField,resource));
        pdfField.setBottomY(countBottomPosY(pdfField,resource));
        pdfField.countMultiLineHeight(fontSize, resource);
    }

    protected Set<I18nString> resolveOptions(Map<String, I18nString> options){
        Set<I18nString> result = new HashSet<>();
        options.forEach((k, v) -> {
            result.add(new I18nString(k, v.getDefaultValue()));
        });
        return result;
    }
}
