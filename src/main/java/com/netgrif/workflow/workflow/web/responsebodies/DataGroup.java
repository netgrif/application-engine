package com.netgrif.workflow.workflow.web.responsebodies;

import lombok.Data;

@Data
public class DataGroup {

    private DataFieldsResource fields;

    private Integer cols;

    private String title;

    private String alignment;

    private Boolean stretch;

    private DataGroup() {
    }

    public DataGroup(DataFieldsResource fields, String title, String alignment, Boolean stretch, Integer cols) {
        this();
        this.fields = fields;
        this.title = title;
        this.alignment = alignment;
        this.stretch = stretch;
        this.cols = cols;
    }
}