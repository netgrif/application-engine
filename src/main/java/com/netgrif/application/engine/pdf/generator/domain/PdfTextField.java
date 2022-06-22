package com.netgrif.application.engine.pdf.generator.domain;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.petrinet.domain.DataGroup;

import java.util.ArrayList;
import java.util.List;

public class PdfTextField extends PdfField {

    public PdfTextField(String id, DataGroup dataGroup, DataType type, String label, String value, PdfResource resource) {
        super(resource);
        this.fieldId = id;
        this.dataGroup = dataGroup;
        this.type = type;
        this.label = label;
        this.values = new ArrayList<>();
        this.values.add(value);
    }

    public PdfTextField(String fieldId, DataGroup dataGroup, String label, DataType type,
                        int x, int bottomY, int width, int height, PdfResource resource) {
        super(resource);
        this.fieldId = fieldId;
        this.dataGroup = dataGroup;
        this.label = label;
        this.values = new ArrayList<>();
        this.type = type;
        this.x = x;
        this.originalTopY = bottomY - height;
        this.topY = bottomY - height;
        this.originalBottomY = bottomY;
        this.bottomY = bottomY;
        this.width = width;
        this.height = height;
    }

    public PdfTextField(String fieldId, String label, List<String> values, DataType type,
                        int x, int bottomY, int width, int height, PdfResource resource) {
        super(resource);
        this.fieldId = fieldId;
        this.label = label;
        this.values = values;
        this.type = type;
        this.x = x;
        this.bottomY = bottomY;
        this.width = width;
        this.height = height;
    }
}
