package api.petrinet.domain;

import api.petrinet.domain.events.DataEventDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;

import java.util.Map;
import java.util.Set;

public final class DataFieldLogicDto {

    private Set<String> behavior;

    private Map<String, DataEventDto> events;

    private FieldLayoutDto layout;

    private ComponentDto component;

    public DataFieldLogicDto() {
    }

    public DataFieldLogicDto(Set<String> behavior, Map<String, DataEventDto> events, FieldLayoutDto layout, ComponentDto component) {
        this.behavior = behavior;
        this.events = events;
        this.layout = layout;
        this.component = component;
    }

    public Set<String> getBehavior() {
        return behavior;
    }

    public void setBehavior(Set<String> behavior) {
        this.behavior = behavior;
    }

    public Map<String, DataEventDto> getEvents() {
        return events;
    }

    public void setEvents(Map<String, DataEventDto> events) {
        this.events = events;
    }

    public FieldLayoutDto getLayout() {
        return layout;
    }

    public void setLayout(FieldLayoutDto layout) {
        this.layout = layout;
    }

    public ComponentDto getComponent() {
        return component;
    }

    public void setComponent(ComponentDto component) {
        this.component = component;
    }
}
