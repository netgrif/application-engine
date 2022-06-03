package api.workflow.web.responsebodies;

import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.dataset.logic.validation.LocalisedValidationDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.List;
import java.util.Map;

public abstract class LocalisedMapOptionsFieldDto<DV> extends LocalisedFieldDto {

    private Map<String, String> options;

    public LocalisedMapOptionsFieldDto() {
    }

    public LocalisedMapOptionsFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
    }

    public LocalisedMapOptionsFieldDto(Map<String, String> options) {
        this.options = options;
    }

    public LocalisedMapOptionsFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId, Map<String, String> options) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
        this.options = options;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
}
