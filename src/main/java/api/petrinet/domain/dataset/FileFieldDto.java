package api.petrinet.domain.dataset;

import api.petrinet.domain.dataset.logic.action.runner.ExpressionDto;
import api.petrinet.domain.dataset.logic.validation.ValidationDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.I18nStringDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;
import api.petrinet.domain.events.DataEventDto;

import java.util.List;
import java.util.Map;

public final class FileFieldDto extends FieldDto<FileFieldValueDto> {

    private Boolean remote;

    public FileFieldDto() {
    }

    public FileFieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, FileFieldValueDto value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, FileFieldValueDto defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
    }

    public FileFieldDto(Boolean remote) {
        this.remote = remote;
    }

    public FileFieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, FileFieldValueDto value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, FileFieldValueDto defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId, Boolean remote) {
        super(id, type, name, description, placeholder, behavior, layout, value, order, immediate, events, encryption, length, component, defaultValue, initExpression, validations, parentTaskId, parentCaseId);
        this.remote = remote;
    }

    public Boolean getRemote() {
        return remote;
    }

    public void setRemote(Boolean remote) {
        this.remote = remote;
    }
}
