package com.netgrif.application.engine.petrinet.domain.events;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.List;

@Data
public class BaseEvent extends Imported {

    private String id;

    private I18nString title;

    private I18nString message;

    private List<Action> preActions;

    private List<Action> postActions;
}
