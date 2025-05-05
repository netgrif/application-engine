package com.netgrif.application.engine.authorization.domain.params;

import com.netgrif.application.engine.authorization.domain.constants.ActorConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public abstract class ActorParams implements CaseParams {

    protected CaseField groupIds;

    protected ActorParams(CaseField groupIds) {
        this.groupIds = groupIds;
    }

    /**
     * todo javadoc
     * */
    protected DataSet toDataSetInternal(@NotNull DataSet dataSet) {
        return dataSet;
    }

    @Override
    public DataSet toDataSet() {
        DataSet dataSet = new DataSet();

        dataSet.put(ActorConstants.GROUPS_FIELD_ID, this.groupIds);

        return toDataSetInternal(dataSet);
    }
}
