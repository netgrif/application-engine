package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@Data
@JsonRootName("dataGroup")
public class LocalisedDataGroup {

    private DataFieldsResource fields;

    private String title;

    private String alignment;

    private Boolean stretch;

    private LocalisedDataGroup() {
    }

    public LocalisedDataGroup(DataFieldsResource fields, String title, String alignment, Boolean stretch) {
        this();
        this.fields = fields;
        this.title = title;
        this.alignment = alignment;
        this.stretch = stretch;
    }
}