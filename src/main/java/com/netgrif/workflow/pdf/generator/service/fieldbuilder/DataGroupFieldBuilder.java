package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfDataGroupField;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.petrinet.domain.DataGroup;

public class DataGroupFieldBuilder extends FieldBuilder {

    public DataGroupFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, PdfField pdfField){
        PdfField dgField = new PdfDataGroupField(dataGroup.getImportId(), 0, pdfField.getLayoutY(),
                resource.getPageDrawableWidth(), resource.getFormGridRowHeight() - resource.getPadding(), dataGroup.getTitle().toString(), true, resource);
        setFieldPositions(dgField, resource.getFontGroupSize());
        return dgField;
    }
}
