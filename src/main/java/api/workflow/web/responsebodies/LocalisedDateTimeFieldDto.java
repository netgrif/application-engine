package api.workflow.web.responsebodies;

import api.petrinet.domain.dataset.logic.validation.LocalisedValidationDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.List;

public final class LocalisedDateTimeFieldDto extends LocalisedFieldDto {

    private String minDate;

    private String maxDate;

    public LocalisedDateTimeFieldDto() {
    }

    public LocalisedDateTimeFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
    }

    public LocalisedDateTimeFieldDto(String minDate, String maxDate) {
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public LocalisedDateTimeFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId, String minDate, String maxDate) {
        super(stringId, type, name, description, placeholder, behavior, layout, value, order, length, component, validations, parentTaskId, parentCaseId);
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public String getMinDate() {
        return minDate;
    }

    public void setMinDate(String minDate) {
        this.minDate = minDate;
    }

    public String getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(String maxDate) {
        this.maxDate = maxDate;
    }
}
