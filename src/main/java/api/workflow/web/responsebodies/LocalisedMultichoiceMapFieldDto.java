package api.workflow.web.responsebodies;

import api.petrinet.domain.dataset.logic.validation.LocalisedValidationDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LocalisedMultichoiceMapFieldDto extends LocalisedMapOptionsFieldDto<Set<String>> {

    public LocalisedMultichoiceMapFieldDto() {
    }

    public LocalisedMultichoiceMapFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
    }

    public LocalisedMultichoiceMapFieldDto(Map<String, String> options) {
        super(options);
    }

    public LocalisedMultichoiceMapFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId, Map<String, String> options) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId, options);
    }
}
