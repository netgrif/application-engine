package com.netgrif.workflow.pdf.generator.domain;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class PdfTaskRefField extends PdfField {

    @Getter
    @Setter
    private int rows;

    @Getter
    @Setter
    private List<String> pdfFieldIds;

    public PdfTaskRefField(PdfResource resource) {
        super(resource);
        this.pdfFieldIds = new ArrayList<>();

    }

    public PdfTaskRefField(String id, DataGroup dataGroup, FieldType type, String label, List<String> value, PdfResource resource){
        super(resource);
        this.fieldId = id;
        this.dataGroup = dataGroup;
        this.type = type;
        this.label = label;
        this.values = value;
        this.pdfFieldIds = new ArrayList<>();
    }

    @Override
    public int compareTo(PdfField pdfField) {
        return Integer.compare(this.getLayoutY(), pdfField.getLayoutY());
    }
}
