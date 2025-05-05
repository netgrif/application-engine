package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.domain.constants.ActorConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class Actor extends SystemCase {

    public Actor(Case actorCase) {
        super(actorCase);
    }

    public abstract String getName();

    public List<String> getGroupIds() {
        return ((CaseField) getCase().getDataSet().get(ActorConstants.GROUPS_FIELD_ID)).getRawValue();
    }

}
