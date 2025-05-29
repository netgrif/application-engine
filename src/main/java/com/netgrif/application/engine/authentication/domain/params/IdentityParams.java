package com.netgrif.application.engine.authentication.domain.params;

import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class IdentityParams extends CaseParams {
    protected TextField username;
    protected TextField firstname;
    protected TextField lastname;
    protected TextField password;
    protected EnumerationMapField state;
    protected DateTimeField expirationDateTime;
    protected TextField registrationToken;
    protected CaseField mainActor;
    protected CaseField additionalActors;

    @Builder(builderMethodName = "with")
    public IdentityParams(Map<String, String> properties, TextField username, TextField firstname, TextField lastname,
                          TextField password, EnumerationMapField state, DateTimeField expirationDateTime,
                          TextField registrationToken, CaseField mainActor, CaseField additionalActors) {
        super(properties);
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.state = state;
        this.expirationDateTime = expirationDateTime;
        this.registrationToken = registrationToken;
        this.mainActor = mainActor;
        this.additionalActors = additionalActors;
    }

    @Override
    public DataSet toDataSet() {
        DataSet dataSet = new DataSet();

        dataSet.put(IdentityConstants.USERNAME_FIELD_ID, this.username);
        dataSet.put(IdentityConstants.FIRSTNAME_FIELD_ID, this.firstname);
        dataSet.put(IdentityConstants.LASTNAME_FIELD_ID, this.lastname);
        dataSet.put(IdentityConstants.PASSWORD_FIELD_ID, this.password);
        if (this.canForceActiveState()) {
            dataSet.put(IdentityConstants.STATE_FIELD_ID, new TextField(IdentityState.ACTIVE.name().toLowerCase()));
        } else {
            if (this.state != null && this.state.getRawValue() != null) {
                this.state.setRawValue(this.state.getRawValue().toLowerCase());
            }
            dataSet.put(IdentityConstants.STATE_FIELD_ID, this.state);
        }
        dataSet.put(IdentityConstants.EXPIRATION_DATE_FIELD_ID, this.expirationDateTime);
        dataSet.put(IdentityConstants.REGISTRATION_TOKEN_FIELD_ID, this.registrationToken);
        dataSet.put(IdentityConstants.MAIN_ACTOR_FIELD_ID, this.mainActor);
        dataSet.put(IdentityConstants.ADDITIONAL_ACTORS_FIELD_ID, this.additionalActors);

        return dataSet;
    }

    @Override
    public String targetProcessIdentifier() {
        return IdentityConstants.PROCESS_IDENTIFIER;
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

    /**
     * todo javadoc
     * */
    protected boolean canForceActiveState() {
        return !hasFieldAnyValue(this.state)
                && hasFieldAnyValue(this.username) && hasFieldAnyValue(this.password) && hasFieldAnyValue(this.mainActor);
    }

    /**
     * todo javadoc
     * */
    protected boolean hasFieldAnyValue(Field<?> field) {
        return field != null && field.getRawValue() != null;
    }
}
