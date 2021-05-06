package com.netgrif.workflow.pdf.generator.domain;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;

import java.util.ArrayList;
import java.util.List;

public class PdfTextField extends PdfField{

    public PdfTextField(String id, DataGroup dataGroup, FieldType type, String label, String value, PdfResource resource){
        super(resource);
        this.fieldId = id;
        this.dataGroup = dataGroup;
        this.type = type;
        this.label = label;
        this.values = new ArrayList<>();
        this.values.add(value);
    }

    public PdfTextField(String fieldId, DataGroup dataGroup, String label, FieldType type,
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

    public PdfTextField(String fieldId, String label, List<String> values, FieldType type,
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
