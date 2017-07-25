package com.netgrif.workflow.workflow.web.responsebodies;

import lombok.Data;

@Data
public class TaskReference {

    private String objectId;

    private String title;

    private String transitionId;

    public TaskReference(String objectId, String title, String transitionId) {
        this.objectId = objectId;
        this.title = title;
        this.transitionId = transitionId;
    }
}