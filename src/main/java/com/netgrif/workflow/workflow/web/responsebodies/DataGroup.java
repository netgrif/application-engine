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

    private String parentTaskId;

    private String parentTaskRefId;

    private DataGroup() {
    }

    public DataGroup(DataFieldsResource fields, String title, String alignment, Boolean stretch, DataGroupLayout layout, String parentTaskId, String parentTaskRefId) {
        this();
        this.fields = fields;
        this.title = title;
        this.alignment = alignment;
        this.stretch = stretch;
        this.layout = layout;
        this.parentTaskId = parentTaskId;
        this.parentTaskRefId = parentTaskRefId;
    }
}