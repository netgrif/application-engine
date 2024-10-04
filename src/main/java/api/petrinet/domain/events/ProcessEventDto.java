package api.petrinet.domain.events;

import api.petrinet.domain.dataset.logic.action.ActionDto;
import api.petrinet.domain.I18nStringDto;

import java.util.List;

public final class ProcessEventDto extends BaseEventDto {

    private String type;

    public ProcessEventDto() {
    }

    public ProcessEventDto(String id, I18nStringDto title, I18nStringDto message, List<ActionDto> preActions, List<ActionDto> postActions) {
        super(id, title, message, preActions, postActions);
    }

    public ProcessEventDto(String type) {
        this.type = type;
    }

    public ProcessEventDto(String id, I18nStringDto title, I18nStringDto message, List<ActionDto> preActions, List<ActionDto> postActions, String type) {
        super(id, title, message, preActions, postActions);
        this.type = type;
    }
}
