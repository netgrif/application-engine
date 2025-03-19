package com.netgrif.application.engine.authentication.domain.params;

import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "with")
public class IdentityParams implements CaseParams {
    protected TextField username;
    protected TextField firstname;
    protected TextField lastname;
    protected TextField password;
    protected EnumerationMapField state;
    protected DateTimeField expirationDateTime;
    protected TextField registrationToken;
    protected CaseField mainActor;
    protected CaseField additionalActors;

    @Override
    public DataSet toDataSet() {
        DataSet dataSet = new DataSet();

        dataSet.put(IdentityConstants.USERNAME_FIELD_ID, this.username);
        dataSet.put(IdentityConstants.FIRSTNAME_FIELD_ID, this.firstname);
        dataSet.put(IdentityConstants.LASTNAME_FIELD_ID, this.lastname);
        dataSet.put(IdentityConstants.PASSWORD_FIELD_ID, this.password);
        dataSet.put(IdentityConstants.STATE_FIELD_ID, this.state);
        dataSet.put(IdentityConstants.EXPIRATION_DATE_FIELD_ID, this.expirationDateTime);
        dataSet.put(IdentityConstants.REGISTRATION_TOKEN_FIELD_ID, this.registrationToken);
        dataSet.put(IdentityConstants.MAIN_ACTOR_FIELD_ID, this.mainActor);
        dataSet.put(IdentityConstants.ADDITIONAL_ACTORS_FIELD_ID, this.additionalActors);

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

    /**
     * todo javadoc
     * */
    public String getPassword() {
        if (this.password == null) {
            return null;
        }
        return this.password.getRawValue();
    }
}
