package com.netgrif.workflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TextField extends Field<String> {

    public static final String SIMPLE_SUBTYPE = "simple";
    public static final String AREA_SUBTYPE = "area";

    private String subType;
    @Transient
    private String value;

    public TextField() {
        super();
    }

    public TextField(String[] values){
        this();
        this.subType = values != null ? values[0] : SIMPLE_SUBTYPE;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void setType(FieldType type){
        this.type = FieldType.TEXT;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }
}