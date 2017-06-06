package com.netgrif.workflow.importer.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Data
public class Document {

    @XmlElement(name = "arc")
    private ImportArc[] arcs;

    @XmlElement(name = "data")
    private ImportData[] data;

    @XmlElement(name = "role")
    private ImportRole[] roles;

    @XmlElement(name = "transition")
    private ImportTransition[] transitions;

    @XmlElement(name = "place")
    private ImportPlace[] places;
}
