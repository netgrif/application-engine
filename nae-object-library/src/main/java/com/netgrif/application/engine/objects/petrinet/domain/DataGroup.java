package com.netgrif.application.engine.objects.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.objects.petrinet.domain.layout.DataGroupLayout;
import com.netgrif.application.engine.objects.workflow.domain.DataFieldsCollection;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Setter
@Getter
public abstract class DataGroup extends PetriNetObject {

    @JsonIgnore
    private Set<String> data;

    private DataFieldsCollection<?> fields;

    private DataGroupLayout layout;

    private I18nString title;

    private String alignment;

    private Boolean stretch;

    private String parentTaskId;

    private String parentTransitionId;

    private String parentCaseId;

    private String parentTaskRefId;

    private int nestingLevel;

    public DataGroup() {
        this._id = new ObjectId();
        this.data = new LinkedHashSet<>();
    }

    public DataGroup(DataGroup dataGroup) {
        this.importId = dataGroup.getImportId();
        this.title = dataGroup.getTitle();
        this.data = dataGroup.getData();
        this.fields = dataGroup.getFields();
        this.layout = dataGroup.getLayout();
        this.alignment = dataGroup.getAlignment();
        this.stretch = dataGroup.getStretch();
        this.parentTaskId = dataGroup.getParentTaskId();
        this.parentTransitionId = dataGroup.getParentTransitionId();
        this.parentCaseId = dataGroup.getParentCaseId();
        this.parentTaskRefId = dataGroup.getParentTaskRefId();
        this.nestingLevel = dataGroup.getNestingLevel();
    }

    public void addData(String dataId) {
        data.add(dataId);
    }

    public String getTranslatedTitle(Locale locale) {
        if (title == null)
            return null;
        return title.getTranslation(locale);
    }
}
