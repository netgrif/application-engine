package api.petrinet.domain.dataset;

import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.dataset.logic.action.runner.ExpressionDto;
import api.petrinet.domain.dataset.logic.validation.ValidationDto;
import api.petrinet.domain.events.DataEventDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.I18nStringDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.List;
import java.util.Map;

public abstract class MapOptionsFieldDto<T,U> extends FieldDto<U> {

    private Map<String, T> options;

    private ExpressionDto optionsExpression;

    public MapOptionsFieldDto() {
    }

    public MapOptionsFieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, U value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, U defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
    }

    public MapOptionsFieldDto(Map<String, T> options, ExpressionDto optionsExpression) {
        this.options = options;
        this.optionsExpression = optionsExpression;
    }

    public MapOptionsFieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, U value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, U defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId, Map<String, T> options, ExpressionDto optionsExpression) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
        this.options = options;
        this.optionsExpression = optionsExpression;
    }

    public Map<String, T> getOptions() {
        return options;
    }

    public void setOptions(Map<String, T> options) {
        this.options = options;
    }

    public ExpressionDto getOptionsExpression() {
        return optionsExpression;
    }

    public void setOptionsExpression(ExpressionDto optionsExpression) {
        this.optionsExpression = optionsExpression;
    }
}
