package com.netgrif.application.engine.authorization.domain.params;

import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserParams extends ActorParams {
    protected TextField email;
    protected TextField firstname;
    protected TextField lastname;

    @Builder(builderMethodName = "with")
    private UserParams(CaseField groupIds, TextField email, TextField firstname, TextField lastname) {
        super(groupIds);
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    /**
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

    @Override
    protected DataSet toDataSetInternal(DataSet dataSet) {
        dataSet.put(UserConstants.EMAIL_FIELD_ID, this.email);
        dataSet.put(UserConstants.FIRSTNAME_FIELD_ID, this.firstname);
        dataSet.put(UserConstants.LASTNAME_FIELD_ID, this.lastname);

        return dataSet;
    }

    @Override
    public String targetProcessIdentifier() {
        return UserConstants.PROCESS_IDENTIFIER;
    }
}
