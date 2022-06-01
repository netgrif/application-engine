package api.petrinet.domain.dataset;

import api.petrinet.domain.dataset.logic.action.runner.ExpressionDto;
import api.petrinet.domain.dataset.logic.validation.ValidationDto;
import api.petrinet.domain.events.DataEventDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.I18nStringDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class MultichoiceMapFieldDto extends MapOptionsFieldDto<I18nStringDto, HashSet<String>> {

    public MultichoiceMapFieldDto() {
    }

    public MultichoiceMapFieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, HashSet<String> value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, HashSet<String> defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
    }

    public MultichoiceMapFieldDto(Map<String, I18nStringDto> options, ExpressionDto optionsExpression) {
        super(options, optionsExpression);
    }

    public MultichoiceMapFieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, HashSet<String> value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, HashSet<String> defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId, Map<String, I18nStringDto> options, ExpressionDto optionsExpression) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId, options, optionsExpression);
    }
}
