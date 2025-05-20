package com.netgrif.application.engine.authorization.domain.params;

import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
public class GroupParams extends ActorParams {
    protected TextField name;
    @Setter
    protected CaseField parentGroupId;

    @Builder(builderMethodName = "with")
    private GroupParams(CaseField groupIds, Map<String, String> properties, TextField name, CaseField parentGroupId) {
        super(groupIds, properties);
        this.name = name;
        this.parentGroupId = parentGroupId;
    }


    @Override
    protected DataSet toDataSetInternal(@NotNull DataSet dataSet) {
        dataSet.put(GroupConstants.NAME_FIELD_ID, this.name);
        dataSet.put(GroupConstants.PARENT_GROUP_FIELD_ID, this.parentGroupId);

        return dataSet;
    }

    @Override
    public String targetProcessIdentifier() {
        return GroupConstants.PROCESS_IDENTIFIER;
    }
}
