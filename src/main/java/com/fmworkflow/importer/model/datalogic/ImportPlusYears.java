package com.fmworkflow.importer.model.datalogic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImportPlusYears {
    @XmlValue
    private Integer content;
    @XmlAttribute
    private Long ref;

    public Integer getContent() {
        return content;
    }

    public void setContent(Integer value) {
        this.content = value;
    }

    public Long getRef() {
        return ref;
    }

    public void setRef(Long ref) {
        this.ref = ref;
    }
}