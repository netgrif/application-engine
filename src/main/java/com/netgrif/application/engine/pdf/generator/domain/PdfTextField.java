package com.netgrif.application.engine.pdf.generator.domain;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

public class PdfTextField extends PdfField<List<String>> {

    public PdfTextField(String id, DataGroup dataGroup, DataType type) {
        super(id, dataGroup, type);
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
