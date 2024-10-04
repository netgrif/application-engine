package api.petrinet.domain.events;

import api.petrinet.domain.dataset.logic.action.ActionDto;
import api.petrinet.domain.I18nStringDto;
import api.petrinet.domain.ImportedDto;

import java.util.List;

public abstract class BaseEventDto extends ImportedDto {

    private String id;

    private I18nStringDto title;

    private I18nStringDto message;

    private List<ActionDto> preActions;

    private List<ActionDto> postActions;

    public BaseEventDto() {
    }

    public BaseEventDto(String id, I18nStringDto title, I18nStringDto message, List<ActionDto> preActions, List<ActionDto> postActions) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.preActions = preActions;
        this.postActions = postActions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public I18nStringDto getTitle() {
        return title;
    }

    public void setTitle(I18nStringDto title) {
        this.title = title;
    }

    public I18nStringDto getMessage() {
        return message;
    }

    public void setMessage(I18nStringDto message) {
        this.message = message;
    }

    public List<ActionDto> getPreActions() {
        return preActions;
    }

    public void setPreActions(List<ActionDto> preActions) {
        this.preActions = preActions;
    }

    public List<ActionDto> getPostActions() {
        return postActions;
    }

    public void setPostActions(List<ActionDto> postActions) {
        this.postActions = postActions;
    }
}
