package com.netgrif.application.engine.workflow.web.responsebodies;

import lombok.Data;

@Data
public class TaskReference {

    private String stringId;

    private String title;

    private String transitionId;

    public TaskReference(String stringId, String title, String transitionId) {
        this.stringId = stringId;
        this.title = title;
        this.transitionId = transitionId;
    }
}