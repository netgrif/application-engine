package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.layout.DataGroupLayout;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class DataGroup {

    // TODO: NAE-1645 check frontend compatibility
    private LinkedHashMap<String, DataRef> fields;

    private DataGroupLayout layout;

    private String title;

    private String alignment;

    private Boolean stretch;

    private String parentTaskId;

    private String parentTransitionId;

    private String parentCaseId;

    private String parentTaskRefId;

    private int nestingLevel;

    private DataGroup() {
    }

    public DataGroup(LinkedHashMap<String, DataRef> fields, String title, String alignment, Boolean stretch, DataGroupLayout layout, String parentTaskId, String parentCaseId, String parentTaskRefId, int nestingLevel) {
        this();
        this.fields = fields;
        this.title = title;
        this.alignment = alignment;
        this.stretch = stretch;
        this.layout = layout;
        this.parentTaskId = parentTaskId;
        this.parentCaseId = parentCaseId;
        this.parentTaskRefId = parentTaskRefId;
        this.nestingLevel = nestingLevel;
    }
}