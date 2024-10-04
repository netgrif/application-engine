package api.petrinet.domain;

import api.workflow.web.responsebodies.LocalisedFieldDto;

import java.util.Collection;

public final class DataFieldsResourceDto {

    private Collection<LocalisedFieldDto> content;

    public DataFieldsResourceDto() {
    }

    public DataFieldsResourceDto(Collection<LocalisedFieldDto> content) {
        this.content = content;
    }

    public Collection<LocalisedFieldDto> getContent() {
        return content;
    }

    public void setContent(Collection<LocalisedFieldDto> content) {
        this.content = content;
    }
}
