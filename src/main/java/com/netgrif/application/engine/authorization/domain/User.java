package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.workflow.domain.Case;

/**
 * todo javadoc
 * */
public class User implements Actor {

    private final Case userCase;

    public User(Case userCase) {
        this.userCase = userCase;
    }

    @Override
    public Case getCase() {
        return this.userCase;
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
        return (String) userCase.getDataSet().get(UserConstants.EMAIL_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getFirstname() {
        return (String) userCase.getDataSet().get(UserConstants.FIRSTNAME_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getLastname() {
        return (String) userCase.getDataSet().get(UserConstants.LASTNAME_FIELD_ID).getRawValue();
    }
}
