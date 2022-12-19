package com.netgrif.application.engine.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.importer.model.DataGroupAlignment;
import com.netgrif.application.engine.petrinet.domain.layout.DataGroupLayout;
import com.netgrif.application.engine.workflow.web.responsebodies.DataFieldsResource;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Data
@Document
public class DataGroup extends PetriNetObject {

    @JsonIgnore
    private Set<String> data;

    @Transient
    private LinkedHashMap<String, DataRef> fields; // TODO: NAE-1645 datarefs

    private DataGroupLayout layout;

    private I18nString title;

    private DataGroupAlignment alignment; // TODO: NAE-1645 enum?

    private Boolean stretch;

    @Transient
    private String parentTaskId; // TODO: NAE-1645 remove?

    @Transient
    private String parentTransitionId; // TODO: NAE-1645 remove?

    @Transient
    private String parentCaseId; // TODO: NAE-1645 remove?

    @Transient
    private String parentTaskRefId; // TODO: NAE-1645 remove?

    @Transient
    private int nestingLevel; // TODO: NAE-1645 remove?

    public DataGroup() {
        this._id = new ObjectId();
        this.data = new LinkedHashSet<>();
    }

    public void addData(String dataId) {
        data.add(dataId);
    }

    public String getTranslatedTitle(Locale locale) {
        if (title == null)
            return null;
        return title.getTranslation(locale);
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