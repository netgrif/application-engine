package api.workflow.web.responsebodies;

import api.petrinet.domain.dataset.logic.validation.LocalisedValidationDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.List;

public class LocalisedEnumerationFieldDto extends LocalisedChoiceFieldDto {

    public LocalisedEnumerationFieldDto() {
    }

    public LocalisedEnumerationFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
    }

    public LocalisedEnumerationFieldDto(List<String> choices) {
        super(choices);
    }

    public LocalisedEnumerationFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId, List<String> choices) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId, choices);
    }
}
