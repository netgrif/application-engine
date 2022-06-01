package api.petrinet.domain;

import api.petrinet.domain.layout.DataGroupLayoutDto;

import java.util.Set;

public class DataGroupDto extends PetriNetObjectDto {

    private Set<String> data;

    private DataFieldsResourceDto fields;

    private DataGroupLayoutDto layout;

    private I18nStringDto title;

    private String alignment;

    private Boolean stretch;

    private String parentTaskId;

    private String parentTransitionId;

    private String parentCaseId;

    private String parentTaskRefId;

    private int nestingLevel;

    public DataGroupDto() {
    }

    public DataGroupDto(Set<String> data, DataFieldsResourceDto fields, DataGroupLayoutDto layout, I18nStringDto title, String alignment, Boolean stretch, String parentTaskId, String parentTransitionId, String parentCaseId, String parentTaskRefId, int nestingLevel) {
        this.data = data;
        this.fields = fields;
        this.layout = layout;
        this.title = title;
        this.alignment = alignment;
        this.stretch = stretch;
        this.parentTaskId = parentTaskId;
        this.parentTransitionId = parentTransitionId;
        this.parentCaseId = parentCaseId;
        this.parentTaskRefId = parentTaskRefId;
        this.nestingLevel = nestingLevel;
    }

    public Set<String> getData() {
        return data;
    }

    public void setData(Set<String> data) {
        this.data = data;
    }

    public DataFieldsResourceDto getFields() {
        return fields;
    }

    public void setFields(DataFieldsResourceDto fields) {
        this.fields = fields;
    }

    public DataGroupLayoutDto getLayout() {
        return layout;
    }

    public void setLayout(DataGroupLayoutDto layout) {
        this.layout = layout;
    }

    public I18nStringDto getTitle() {
        return title;
    }

    public void setTitle(I18nStringDto title) {
        this.title = title;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public Boolean getStretch() {
        return stretch;
    }

    public void setStretch(Boolean stretch) {
        this.stretch = stretch;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getParentTransitionId() {
        return parentTransitionId;
    }

    public void setParentTransitionId(String parentTransitionId) {
        this.parentTransitionId = parentTransitionId;
    }

    public String getParentCaseId() {
        return parentCaseId;
    }

    public void setParentCaseId(String parentCaseId) {
        this.parentCaseId = parentCaseId;
    }

    public String getParentTaskRefId() {
        return parentTaskRefId;
    }

    public void setParentTaskRefId(String parentTaskRefId) {
        this.parentTaskRefId = parentTaskRefId;
    }

    public int getNestingLevel() {
        return nestingLevel;
    }

    public void setNestingLevel(int nestingLevel) {
        this.nestingLevel = nestingLevel;
    }
}
