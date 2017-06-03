package com.netgrif.workflow.importer.model.datalogic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImportAutoPlus {
    @XmlValue
    private String content;
    @XmlAttribute
    private Long ref;

    public String getContent() {
        return content;
    }

    public void setContent(String value) {
        this.content = value;
    }

    public Long getRef() {
        return ref;
    }

    public void setRef(Long ref) {
        this.ref = ref;
    }
}