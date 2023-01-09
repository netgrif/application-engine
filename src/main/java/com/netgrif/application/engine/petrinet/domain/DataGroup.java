package com.netgrif.application.engine.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.importer.model.DataGroupAlignment;
import com.netgrif.application.engine.petrinet.domain.layout.DataGroupLayout;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Document
public class DataGroup extends PetriNetObject {

    @JsonIgnore
    private Set<String> data;

    @Transient
    private LinkedHashMap<String, DataRef> dataRefs;

    private DataGroupLayout layout;

    private I18nString title;

    private DataGroupAlignment alignment;

    private Boolean stretch;

    @Transient
    private String parentTaskId;

    @Transient
    private String parentTransitionId;

    @Transient
    private String parentCaseId;

    @Transient
    private String parentTaskRefId;

    @Transient
    private int nestingLevel;

    public DataGroup() {
        this._id = new ObjectId();
        this.data = new LinkedHashSet<>();
    }

    public void addData(String dataId) {
        data.add(dataId);
    }

    public DataGroup clone() {
        DataGroup group = new DataGroup();
        group.setImportId(this.getImportId());
        group.setTitle(this.getTitle());
        group.setData(this.getData());
        group.setLayout(this.getLayout());
        group.setAlignment(this.getAlignment());
        group.setStretch(this.getStretch());
        return group;
    }
}