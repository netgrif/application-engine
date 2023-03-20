//package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;
//
//import com.netgrif.application.engine.importer.model.DataType;
//import com.netgrif.application.engine.pdf.generator.config.PdfResource;
//import com.netgrif.application.engine.pdf.generator.domain.PdfDataGroupField;
//import com.netgrif.application.engine.pdf.generator.domain.PdfField;
//import com.netgrif.application.engine.petrinet.domain.DataGroup;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PdfDataGroupFieldBuilder extends PdfFieldBuilder {
//
//    public PdfDataGroupFieldBuilder(PdfResource resource) {
//        super(resource);
//    }
//
//    @Override
//    public PdfField buildField(PdfFieldBuildingBlock buildingBlock) {
//        return buildField(buildingBlock.getDataGroup());
//    }
//
//    @Override
//    public DataType getType() {
//        return null;
//    }
//
//    public PdfField buildField(DataGroup dataGroup, PdfField pdfField) {
//        PdfField dgField = new PdfDataGroupField(dataGroup.getImportId(), 0, pdfField.getLayoutY(),
//                resource.getPageDrawableWidth(), resource.getFormGridRowHeight() - resource.getPadding(), dataGroup.getTitle().toString(), true, resource);
//        setFieldPositions(dgField, resource.getFontGroupSize());
//        return dgField;
//    }
//}
