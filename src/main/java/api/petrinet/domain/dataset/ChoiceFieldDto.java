package api.petrinet.domain.dataset;

import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.I18nStringDto;
import api.petrinet.domain.dataset.logic.action.runner.ExpressionDto;
import api.petrinet.domain.dataset.logic.validation.ValidationDto;
import api.petrinet.domain.events.DataEventDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ChoiceFieldDto<T> extends FieldDto<T> {

    private Set<I18nStringDto> choices;

    private ExpressionDto choicesExpression;

    public ChoiceFieldDto() {
    }

    public ChoiceFieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, T value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, T defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
    }

    public ChoiceFieldDto(Set<I18nStringDto> choices, ExpressionDto choicesExpression) {
        this.choices = choices;
        this.choicesExpression = choicesExpression;
    }

    public ChoiceFieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, T value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, T defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId, Set<I18nStringDto> choices, ExpressionDto choicesExpression) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
        this.choices = choices;
        this.choicesExpression = choicesExpression;
    }

    public Set<I18nStringDto> getChoices() {
        return choices;
    }

    public void setChoices(Set<I18nStringDto> choices) {
        this.choices = choices;
    }

    public ExpressionDto getChoicesExpression() {
        return choicesExpression;
    }

    public void setChoicesExpression(ExpressionDto choicesExpression) {
        this.choicesExpression = choicesExpression;
    }
}
