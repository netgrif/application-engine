package com.netgrif.workflow.importer.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ImportData {

    private Long id;

    private String title;

    private String desc;

    private String placeholder;

    @XmlAttribute
    private String type;

    @XmlAttribute
    private boolean immediate;

    private String[] values;

    private Columns columns;

    private String[] valid;

    private String init;

    private ImportAction[] action;

    @XmlElement(name = "documentRef")
    private DocumentRef[] documentRefs;
}