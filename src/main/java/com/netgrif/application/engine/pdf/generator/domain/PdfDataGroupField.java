//package com.netgrif.application.engine.pdf.generator.domain;
//
//import com.netgrif.application.engine.importer.model.DataType;
//import com.netgrif.application.engine.pdf.generator.config.PdfResource;
//
//import java.util.List;
//
//public class PdfDataGroupField extends PdfField {
//
//    public PdfDataGroupField(String fieldId, int layoutX, int layoutY, int width, int originalHeight,
//                             String label, boolean dgField, PdfResource resource) {
//        super(resource);
//        this.fieldId = fieldId;
//        this.layoutX = layoutX;
//        this.layoutY = layoutY;
//        this.width = width;
//        this.height = originalHeight;
//        this.label = label;
//        this.dgField = dgField;
//    }
//
//    public PdfDataGroupField(String fieldId, String label, List<String> values, DataType type,
//                             int x, int bottomY, int width, int height, PdfResource resource) {
//        super(resource);
//        this.fieldId = fieldId;
//        this.label = label;
//        this.value = values;
//        this.type = type;
//        this.x = x;
//        this.bottomY = bottomY;
//        this.width = width;
//        this.height = height;
//    }
//}
