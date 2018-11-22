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
    private String formatting

    TextField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.TEXT
    }

    TextField(String subtype) {
        this()
        this.subType = subtype != null ? subtype : SIMPLE_SUBTYPE
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

    String getFormatting() {
        return formatting
    }

    void setSubType(String subType) {
        this.subType = subType
    }

    void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength
    }

    void setFormatting(String formatting) {
        this.formatting = formatting
    }

    @Override
    Field clone() {
        TextField clone = new TextField()
        super.clone(clone)

        clone.subType = this.subType
        clone.defaultValue = this.defaultValue
        clone.validationRules = this.validationRules

        return clone
    }
}