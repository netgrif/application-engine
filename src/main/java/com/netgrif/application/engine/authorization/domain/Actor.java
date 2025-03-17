package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.domain.constants.ActorConstants;
import com.netgrif.application.engine.workflow.domain.Case;

/**
 * todo javadoc
 * */
public class Actor extends Case {

    /**
     * todo javadoc
     * */
    public String getFullName() {
        String firstname = (String) getDataSet().get(ActorConstants.FIRSTNAME_FIELD_ID).getRawValue();
        String lastname = (String) getDataSet().get(ActorConstants.LASTNAME_FIELD_ID).getRawValue();
        return String.join(" ", firstname, lastname);
    }

    /**
     * todo javadoc
     * */
    public boolean isActive() {
        return (Boolean) getDataSet().get(ActorConstants.STATE_FIELD_ID).getRawValue();
    }
}
