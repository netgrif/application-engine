package com.fmworkflow.workflow.domain.dataset;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Milan on 5.2.2017.
 */
@Document
public class TextField extends Field {

    private String value;
    private Validation validate;

    public TextField() {
        super();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Validation getValidate() {
        return validate;
    }

    public void setValidate(Validation validate) {
        this.validate = validate;
    }

    enum Validation {
        EMAIL,
        TEXT,
        URL
    }




}
