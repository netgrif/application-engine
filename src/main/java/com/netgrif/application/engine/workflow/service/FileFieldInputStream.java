package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import lombok.Data;

import java.io.InputStream;

@Data
public class FileFieldInputStream {

    private InputStream inputStream;

    private String fileName;

    public FileFieldInputStream(InputStream inputStream, String fileName) {
        this.inputStream = inputStream;
        this.fileName = fileName;
    }

    public FileFieldInputStream(FileField field, InputStream inputStream) {
        this.inputStream = inputStream;
        this.fileName = field.getValue().getName();
    }
}
