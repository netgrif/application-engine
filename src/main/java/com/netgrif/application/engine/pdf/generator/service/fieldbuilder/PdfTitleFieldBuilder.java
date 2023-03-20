//package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;
//
//import com.netgrif.application.engine.pdf.generator.config.PdfResource;
//import com.netgrif.application.engine.pdf.generator.domain.PdfField;
//import com.netgrif.application.engine.pdf.generator.domain.PdfTitleField;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PdfTitleFieldBuilder extends PdfFieldBuilder {
//
//    public PdfTitleFieldBuilder(PdfResource resource) {
//        super(resource);
//    }
//
//    @Override
//    public PdfField buildField(PdfFieldBuildingBlock buildingBlock) {
//        return buildField();
//    }
//
//    public PdfField buildField() {
//        PdfField titleField = new PdfTitleField("titleField", 0, 0, resource.getPageDrawableWidth(),
//                resource.getFormGridRowHeight(), resource.getDocumentTitle(), resource);
//        titleField.setOriginalBottomY(countBottomPosY(titleField, resource));
//        titleField.countMultiLineHeight(resource.getFontTitleSize(), resource);
//        titleField.setHeight(titleField.getHeight() + resource.getLineHeight());
//        titleField.setBottomY(countBottomPosY(titleField, resource));
//        return titleField;
//    }
//}
