package com.netgrif.application.engine.workflow.web.responsebodies;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CaseFieldResponse {

    private String caseId;

    private Map<String, Object> data;

    public CaseFieldResponse() {
        data = new HashMap<>();
    }
}
