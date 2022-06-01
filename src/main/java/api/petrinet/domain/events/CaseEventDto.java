package api.petrinet.domain.events;

import api.petrinet.domain.dataset.logic.action.ActionDto;
import api.petrinet.domain.I18nStringDto;

import java.util.List;

public class CaseEventDto extends BaseEventDto {

    private String type;

    public CaseEventDto() {
    }

    public CaseEventDto(String id, I18nStringDto title, I18nStringDto message, List<ActionDto> preActions, List<ActionDto> postActions) {
        super(id, title, message, preActions, postActions);
    }

    public CaseEventDto(String type) {
        this.type = type;
    }

    public CaseEventDto(String id, I18nStringDto title, I18nStringDto message, List<ActionDto> preActions, List<ActionDto> postActions, String type) {
        super(id, title, message, preActions, postActions);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
