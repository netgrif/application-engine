package api.workflow.web.responsebodies;

import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;
import api.petrinet.domain.dataset.logic.validation.LocalisedValidationDto;

import java.util.List;

public final class LocalisedTextFieldDto extends LocalisedFieldDto {

    public LocalisedTextFieldDto() {
    }

    public LocalisedTextFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
    }
}
