package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
class TextField extends ValidableField<String> {

    public static final String SIMPLE_SUBTYPE = "simple"
    public static final String AREA_SUBTYPE = "area"

    private String subType

    @Transient
    private Integer maxLength

    @Transient
    private String formating

    TextField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.TEXT
    }

    TextField(List<String> values) {
        this()
        this.subType = values != null ? values[0] : SIMPLE_SUBTYPE
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    String getSubType() {
        return subType
    }

    Integer getMaxLength() {
        return maxLength
    }

    void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength
    }

    String getFormating() {
        return formating
    }

    void setFormating(String formating) {
        this.formating = formating
    }
}