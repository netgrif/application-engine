package com.netgrif.application.engine.authorization.domain.params;

import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "with")
public class UserParams implements CaseParams {
    protected TextField email;
    protected TextField firstname;
    protected TextField lastname;

    @Override
    public DataSet toDataSet() {
        DataSet dataSet = new DataSet();

        dataSet.put(UserConstants.EMAIL_FIELD_ID, this.email);
        dataSet.put(UserConstants.FIRSTNAME_FIELD_ID, this.firstname);
        dataSet.put(UserConstants.LASTNAME_FIELD_ID, this.lastname);

        return dataSet;
    }

    /**S
     * todo javadoc
     * */
    public static UserParams fromIdentityParams(IdentityParams identityParams) {
        TextField email = null, firstname = null, lastname = null;

        if (identityParams.getUsername() != null) {
            email = new TextField(identityParams.getUsername().getRawValue());
        }
        if (identityParams.getFirstname() != null) {
            firstname = new TextField(identityParams.getFirstname().getRawValue());
        }
        if (identityParams.getLastname() != null) {
            lastname = new TextField(identityParams.getLastname().getRawValue());
        }

        return UserParams.with()
                .email(email)
                .firstname(firstname)
                .lastname(lastname)
                .build();
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
