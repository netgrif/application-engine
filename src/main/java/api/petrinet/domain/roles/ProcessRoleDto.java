package api.petrinet.domain.roles;

import api.petrinet.domain.I18nStringDto;
import api.petrinet.domain.ImportedDto;
import api.petrinet.domain.events.EventDto;

import java.util.Map;

public class ProcessRoleDto extends ImportedDto {

    private String id;

    private I18nStringDto name;

    private String netId;

    private String description;

    private Map<String, EventDto> events;

    public ProcessRoleDto() {
    }

    public ProcessRoleDto(String importId) {
        super(importId);
    }

    public ProcessRoleDto(String id, I18nStringDto name, String netId, String description, Map<String, EventDto> events) {
        this.id = id;
        this.name = name;
        this.netId = netId;
        this.description = description;
        this.events = events;
    }

    public ProcessRoleDto(String importId, String id, I18nStringDto name, String netId, String description, Map<String, EventDto> events) {
        super(importId);
        this.id = id;
        this.name = name;
        this.netId = netId;
        this.description = description;
        this.events = events;
    }

    public String getStringId() {
        return id;
    }

    public void setStringId(String id) {
        this.id = id;
    }

    public I18nStringDto getName() {
        return name;
    }

    public void setName(I18nStringDto name) {
        this.name = name;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, EventDto> getEvents() {
        return events;
    }

    public void setEvents(Map<String, EventDto> events) {
        this.events = events;
    }
}
