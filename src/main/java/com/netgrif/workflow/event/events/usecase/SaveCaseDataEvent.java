package com.netgrif.workflow.event.events.usecase;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SaveCaseDataEvent extends CaseEvent {

    private static final Logger log = Logger.getLogger(SaveCaseDataEvent.class);

    @Getter
    private Collection<ChangedField> data;

    @Getter
    private Map<String, String> userData;

    public SaveCaseDataEvent(Case useCase, ObjectNode values, Collection<ChangedField> data) {
        super(useCase);
        this.data = data;
        this.userData = new HashMap<>();
        values.fields().forEachRemaining(entry -> {
            try {
                userData.put(entry.getKey(), entry.getValue().get("value").asText());
            } catch (Exception e) {
                log.error("Could not parse ["+entry+"]");
            }
        });
    }

    @Override
    public String getMessage() {
        return "New data saved in case " + getCase().getTitle() + " on " + DateUtils.toString(time);
    }
}