package com.netgrif.workflow.importer.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ImportData {

    private Long id;

    private String title;

    private String desc;

    @XmlAttribute
    private String type;

    private String[] values;

    private Columns columns;

    private String[] valid;

    private String init;

    private String[] logic;
}