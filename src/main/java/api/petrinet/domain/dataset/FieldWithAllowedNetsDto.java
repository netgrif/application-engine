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

public abstract class FieldWithAllowedNetsDto<T> extends FieldDto<T> {

    private List<String> allowedNets;

    public FieldWithAllowedNetsDto() {
    }

    public FieldWithAllowedNetsDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, T value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, T defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
    }

    public FieldWithAllowedNetsDto(List<String> allowedNets) {
        this.allowedNets = allowedNets;
    }

    public FieldWithAllowedNetsDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, T value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, T defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId, List<String> allowedNets) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
        this.allowedNets = allowedNets;
    }

    public List<String> getAllowedNets() {
        return allowedNets;
    }

    public void setAllowedNets(List<String> allowedNets) {
        this.allowedNets = allowedNets;
    }
}
