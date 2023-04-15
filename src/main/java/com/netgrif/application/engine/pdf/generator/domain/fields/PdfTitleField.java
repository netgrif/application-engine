package com.netgrif.application.engine.pdf.generator.domain.fields;

import java.util.List;

public class PdfTitleField extends PdfField<List<String>> {

    public static final String TITLE_TYPE = "title";

    public PdfTitleField() {
        super();
    }

    public PdfTitleField(String id) {
        super(id);
        this.layoutX = 0;
        this.layoutY = 0;
    }

    @Override
    public String getType() {
        return TITLE_TYPE;
    }

    @Override
    public boolean isValueEmpty() {
        return this.value == null || this.value.isEmpty();
    }

    @Override
    public PdfTitleField newInstance() {
        return new PdfTitleField();
    }


//    public PdfTitleField(String fieldId, int layoutX, int layoutY, int width, int height, String label, PdfResource resource) {
//        super(resource);
//        this.fieldId = fieldId;
//        this.layoutX = layoutX;
//        this.layoutY = layoutY;
//        this.width = width;
//        this.height = height;
//        this.label = label;
//    }
//
//    public PdfTitleField(String fieldId, String label, List<String> values, DataType type, int x,
//                         int bottomY, int width, int height, PdfResource resource) {
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
