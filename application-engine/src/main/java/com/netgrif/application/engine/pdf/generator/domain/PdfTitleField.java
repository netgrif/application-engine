package com.netgrif.application.engine.pdf.generator.domain;

import com.netgrif.application.engine.pdf.generator.config.PdfResourceConfigurationProperties;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldType;

import java.util.List;

public class PdfTitleField extends PdfField {

    public PdfTitleField(String fieldId, int layoutX, int layoutY, int width, int height, String label, PdfResourceConfigurationProperties resource) {
        super(resource);
        this.fieldId = fieldId;
        this.layoutX = layoutX;
        this.layoutY = layoutY;
        this.width = width;
        this.height = height;
        this.label = label;
    }

    public PdfTitleField(String fieldId, String label, List<String> values, FieldType type, int x,
                         int bottomY, int width, int height, PdfResourceConfigurationProperties resource) {
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
