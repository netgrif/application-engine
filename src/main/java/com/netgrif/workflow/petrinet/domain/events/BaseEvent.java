package com.netgrif.workflow.petrinet.domain.events;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.Imported;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.List;

@Data
public class BaseEvent extends Imported {

    private I18nString title;

    private I18nString message;

    private List<Action> preActions;

    private List<Action> postActions;
}
