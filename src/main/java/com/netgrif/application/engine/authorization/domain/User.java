package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.workflow.domain.Case;

/**
 * todo javadoc
 * */
public class User extends Actor {

    public User(Case userCase) {
        super(userCase);
    }

    @Override
    protected CanInitializeOutcome canInitialize(Case userCase) {
        return new CanInitializeOutcome("Provided user case is of different process",
                userCase.getProcessIdentifier().equals(UserConstants.PROCESS_IDENTIFIER));
    }

    @Override
    public String getName() {
        return getFullName();
    }

    /**
     * todo javadoc
     * */
    public String getFullName() {
        return String.join(" ", getFirstname(), getLastname());
    }

    /**
     * todo javadoc
     * */
    public String getEmail() {
        return (String) getCase().getDataSet().get(UserConstants.EMAIL_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getFirstname() {
        return (String) getCase().getDataSet().get(UserConstants.FIRSTNAME_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getLastname() {
        return (String) getCase().getDataSet().get(UserConstants.LASTNAME_FIELD_ID).getRawValue();
    }
}
