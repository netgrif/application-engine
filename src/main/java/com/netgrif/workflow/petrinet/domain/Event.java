package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.List;

@Data
public class Event extends Imported {

    private EventType type;

    private I18nString title;

    private I18nString message;

    private List<Action> preActions;

    private List<Action> postActions;
}