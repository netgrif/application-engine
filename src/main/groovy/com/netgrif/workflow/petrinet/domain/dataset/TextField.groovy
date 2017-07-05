package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
public class TextField extends Field<String> {

    public static final String SIMPLE_SUBTYPE = "simple";
    public static final String AREA_SUBTYPE = "area";

    private String subType;
    @Transient
    private int maxLength
    @Transient
    private String formating

    public TextField() {
        super();
    }

    public TextField(String[] values) {
        this();
        this.subType = values != null ? values[0] : SIMPLE_SUBTYPE;
    }

    String getSubType() {
        return subType
    }

    int getMaxLength() {
        return maxLength
    }

    void setMaxLength(int maxLength) {
        this.maxLength = maxLength
    }

    String getFormating() {
        return formating
    }

    void setFormating(String formating) {
        this.formating = formating
    }
}