package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;

import java.util.List;

public class Group extends SystemCase implements Actor {

    public Group(Case groupCase) {
        super(groupCase);
    }

    @Override
    protected CanInitializeOutcome canInitialize(Case groupCase) {
        return new CanInitializeOutcome("Provided group case is of different process",
                groupCase.getProcessIdentifier().equals(GroupConstants.PROCESS_IDENTIFIER));
    }

    @Override
    public String getName() {
        return (String) getCase().getDataSet().get(GroupConstants.NAME_FIELD_ID).getRawValue();
    }

    public List<String> getMemberIds() {
        return ((CaseField) getCase().getDataSet().get(GroupConstants.MEMBERS_FIELD_ID)).getRawValue();
    }

    public String getParentGroupId() {
        List<String> parentGroupIdAsList = ((CaseField) getCase().getDataSet().get(GroupConstants.PARENT_GROUP_FIELD_ID)).getRawValue();
        if (parentGroupIdAsList != null && !parentGroupIdAsList.isEmpty()) {
            return parentGroupIdAsList.get(0);
        }
        return null;
    }

}
