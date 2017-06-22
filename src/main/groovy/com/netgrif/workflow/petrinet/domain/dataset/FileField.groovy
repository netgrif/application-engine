package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
public class FileField extends Field<String> {

    public FileField() {
        super();
    }
}