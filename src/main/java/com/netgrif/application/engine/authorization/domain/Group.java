package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.List;

public class Group implements Actor {

    private final Case groupCase;

    public Group(Case groupCase) {
        this.groupCase = groupCase;
    }

    @Override
    public String getName() {
        return (String) groupCase.getDataSet().get(GroupConstants.NAME_FIELD_ID).getRawValue();
    }

    @Override
    public Case getCase() {
        return groupCase;
    }

    public List<String> getMemberIds() {
        return ((CaseField) groupCase.getDataSet().get(GroupConstants.MEMBERS_FIELD_ID)).getRawValue();
    }

    public String getParentGroupId() {
        List<String> parentGroupIdAsList = ((CaseField) groupCase.getDataSet().get(GroupConstants.PARENT_GROUP_FIELD_ID)).getRawValue();
        if (!parentGroupIdAsList.isEmpty()) {
            return parentGroupIdAsList.get(0);
        }
        return null;
    }

}
