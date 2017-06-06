package com.netgrif.workflow.importer.model.datalogic;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ImportAutoPlus {

    @XmlValue
    private String content;

    @XmlAttribute
    private Long ref;
}