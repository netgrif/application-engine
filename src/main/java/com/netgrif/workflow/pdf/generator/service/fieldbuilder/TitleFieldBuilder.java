package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfTitleField;

public class TitleFieldBuilder extends FieldBuilder {

    public TitleFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField createTitleField(){
        PdfField titleField = new PdfTitleField("titleField", 0, 0, resource.getPageDrawableWidth(),
                resource.getFormGridRowHeight(), resource.getDocumentTitle(), resource);
        titleField.setOriginalBottomY(countBottomPosY(titleField, resource));
        titleField.countMultiLineHeight(resource.getFontTitleSize(), resource);
        titleField.setHeight(titleField.getHeight() + resource.getLineHeight());
        titleField.setBottomY(countBottomPosY(titleField,resource));
        return titleField;
    }
}
