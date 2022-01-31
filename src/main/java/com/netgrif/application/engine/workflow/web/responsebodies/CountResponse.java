package com.netgrif.application.engine.workflow.web.responsebodies;

import lombok.Data;

@Data
public class CountResponse {

    private long count;
    private String entity;

    public CountResponse() {
    }

    public CountResponse(long count, String entity) {
        this.count = count;
        this.entity = entity;
    }

    public static CountResponse caseCount(long count) {
        return new CountResponse(count, "case");
    }

    public static CountResponse taskCount(long count) {
        return new CountResponse(count, "task");
    }
}
