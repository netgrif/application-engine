package api.workflow.web.responsebodies;

import api.petrinet.domain.dataset.logic.validation.LocalisedValidationDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.List;

public class LocalisedFilterFieldDto extends LocalisedFieldDto {

    private List<String> allowedNets;

    private Object filterMetadata;

    public LocalisedFilterFieldDto() {
    }

    public LocalisedFilterFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
    }

    public LocalisedFilterFieldDto(List<String> allowedNets, Object filterMetadata) {
        this.allowedNets = allowedNets;
        this.filterMetadata = filterMetadata;
    }

    public LocalisedFilterFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId, List<String> allowedNets, Object filterMetadata) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
        this.allowedNets = allowedNets;
        this.filterMetadata = filterMetadata;
    }

    public List<String> getAllowedNets() {
        return allowedNets;
    }

    public void setAllowedNets(List<String> allowedNets) {
        this.allowedNets = allowedNets;
    }

    public Object getFilterMetadata() {
        return filterMetadata;
    }

    public void setFilterMetadata(Object filterMetadata) {
        this.filterMetadata = filterMetadata;
    }
}
