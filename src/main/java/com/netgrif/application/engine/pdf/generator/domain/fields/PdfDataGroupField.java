package com.netgrif.application.engine.pdf.generator.domain.fields;

import java.util.List;

public class PdfDataGroupField extends PdfField<List<String>> {

    public static final String DATA_GROUP_TYPE = "dataGroup";

    public PdfDataGroupField() {
        super();
    }

    public PdfDataGroupField(String id) {
        super(id);
        this.layoutX = 0;
        this.layoutY = 0;
    }

    @Override
    public String getType() {
        return DATA_GROUP_TYPE;
    }


    @Override
    public boolean isValueEmpty() {
        return this.value == null || this.value.isEmpty();
    }

    @Override
    public PdfDataGroupField newInstance() {
        return new PdfDataGroupField();
    }

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
}
