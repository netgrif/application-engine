package api.petrinet.domain.events;

import api.petrinet.domain.dataset.logic.action.ActionDto;
import api.petrinet.domain.I18nStringDto;

import java.util.List;

public final class EventDto extends BaseEventDto {

    private String type;

    public EventDto() {
    }

    public EventDto(String id, I18nStringDto title, I18nStringDto message, List<ActionDto> preActions, List<ActionDto> postActions) {
        super(id, title, message, preActions, postActions);
    }

    public EventDto(String type) {
        this.type = type;
    }

    public EventDto(String id, I18nStringDto title, I18nStringDto message, List<ActionDto> preActions, List<ActionDto> postActions, String type) {
        super(id, title, message, preActions, postActions);
        this.type = type;
    }
}
