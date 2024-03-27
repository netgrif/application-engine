package com.netgrif.application.engine.workflow.web.requestbodies.file;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileFieldRequest {

    private String fieldId;

    private String parentTaskId;

    private String fileName;

    public FileFieldRequest() {

    }
}
