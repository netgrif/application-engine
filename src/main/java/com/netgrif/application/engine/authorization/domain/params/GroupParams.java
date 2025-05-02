package com.netgrif.application.engine.authorization.domain.params;

import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "with")
public class GroupParams implements CaseParams {
    protected TextField name;
    protected CaseField memberIds;
    protected CaseField parentGroupId;

    @Override
    public DataSet toDataSet() {
        DataSet dataSet = new DataSet();

        dataSet.put(GroupConstants.NAME_FIELD_ID, this.name);
        dataSet.put(GroupConstants.MEMBERS_FIELD_ID, this.memberIds);
        dataSet.put(GroupConstants.PARENT_GROUP_FIELD_ID, this.parentGroupId);

        return dataSet;
    }
}
