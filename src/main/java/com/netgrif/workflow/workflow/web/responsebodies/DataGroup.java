package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.layout.DataGroupLayout;
import lombok.Data;

@Data
public class DataGroup {

    private DataFieldsResource fields;

    private DataGroupLayout layout;

    private String title;

    private String alignment;

    private Boolean stretch;

    private DataGroup() {
    }

    public DataGroup(DataFieldsResource fields, String title, String alignment, Boolean stretch, DataGroupLayout layout) {
        this();
        this.fields = fields;
        this.title = title;
        this.alignment = alignment;
        this.stretch = stretch;
        this.layout = layout;
    }
}