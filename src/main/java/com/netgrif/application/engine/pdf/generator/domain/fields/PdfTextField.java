package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;

import java.util.List;

public class PdfTextField extends PdfField<List<String>> {

    public PdfTextField() {
        super();
    }

    public PdfTextField(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return DataType.TEXT.value();
    }

    @Override
    public boolean isValueEmpty() {
        return this.value == null || this.value.isEmpty();
    }

    @Override
    public PdfTextField newInstance() {
        return new PdfTextField();
    }

//    public PdfTextField(String fieldId, DataGroup dataGroup, String label, DataType type,
//                        int x, int bottomY, int width, int height, PdfResource resource) {
//        super(resource);
//        this.fieldId = fieldId;
//        this.dataGroup = dataGroup;
//        this.label = label;
//        this.value = new ArrayList<>();
//        this.type = type;
//        this.x = x;
//        this.originalTopY = bottomY - height;
//        this.topY = bottomY - height;
//        this.originalBottomY = bottomY;
//        this.bottomY = bottomY;
//        this.width = width;
//        this.height = height;
//    }
}
