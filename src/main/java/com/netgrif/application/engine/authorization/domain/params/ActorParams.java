package com.netgrif.application.engine.authorization.domain.params;

import com.netgrif.application.engine.authorization.domain.constants.ActorConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "with")
public class ActorParams implements CaseParams {
    protected TextField email;
    protected TextField firstname;
    protected TextField lastname;

    @Override
    public DataSet toDataSet() {
        DataSet dataSet = new DataSet();

        dataSet.put(ActorConstants.EMAIL_FIELD_ID, this.email);
        dataSet.put(ActorConstants.FIRSTNAME_FIELD_ID, this.lastname);
        dataSet.put(ActorConstants.LASTNAME_FIELD_ID, this.firstname);

        return dataSet;
    }

    /**
     * todo javadoc
     * */
    public String getFullName() {
        if (this.firstname == null || this.lastname == null) {
            return "";
        }
        return String.join(" ", this.firstname.getRawValue(), this.lastname.getRawValue());
    }
}
