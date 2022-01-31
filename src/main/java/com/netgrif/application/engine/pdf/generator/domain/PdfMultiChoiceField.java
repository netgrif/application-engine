package com.netgrif.application.engine.pdf.generator.domain;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;

import java.util.List;


public class PdfMultiChoiceField extends PdfSelectionField {

    public PdfMultiChoiceField(String fieldId, DataGroup dataGroup, FieldType type, String label, List<String> values, List<String> choices, PdfResource resource) {
        super(resource);
        this.fieldId = fieldId;
        this.dataGroup = dataGroup;
        this.type = type;
        this.label = label;
        this.values = values;
        this.choices = choices;
    }

    public PdfMultiChoiceField(String fieldId, String label, List<String> values, List<String> choices, FieldType type, int x, int bottomY, int width, int height, PdfResource resource) {
        super(resource);
        this.fieldId = fieldId;
        this.label = label;
        this.values = values;
        this.choices = choices;
        this.type = type;
        this.x = x;
        this.bottomY = bottomY;
        this.width = width;
        this.height = height;
    }
}
