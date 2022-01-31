package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfDataGroupField;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;

public class DataGroupFieldBuilder extends FieldBuilder {

    public DataGroupFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, PdfField pdfField) {
        PdfField dgField = new PdfDataGroupField(dataGroup.getImportId(), 0, pdfField.getLayoutY(),
                resource.getPageDrawableWidth(), resource.getFormGridRowHeight() - resource.getPadding(), dataGroup.getTitle().toString(), true, resource);
        setFieldPositions(dgField, resource.getFontGroupSize());
        return dgField;
    }
}
