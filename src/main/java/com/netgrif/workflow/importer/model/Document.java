package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
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

    public ImportArc[] getImportArc() {
        return arcs;
    }

    public void setImportArc(ImportArc[] importArc) {
        this.arcs = importArc;
    }

    public ImportData[] getImportData() {
        return data;
    }

    public void setImportData(ImportData[] importData) {
        this.data = importData;
    }

    public ImportRole[] getImportRoles() {
        return roles;
    }

    public void setImportRoles(ImportRole[] role) {
        this.roles = role;
    }

    public ImportTransition[] getImportTransitions() {
        return transitions;
    }

    public void setImportTransitions(ImportTransition[] importTransitions) {
        this.transitions = importTransitions;
    }

    public ImportPlace[] getImportPlaces() {
        return places;
    }

    public void setImportPlaces(ImportPlace[] place) {
        this.places = place;
    }
}
