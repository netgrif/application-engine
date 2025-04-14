package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.domain.constants.ActorConstants;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;

/**
 * todo javadoc
 * */
public class Actor implements SystemCase {

    private final Case actorCase;

    public Actor(Case actorCase) {
        this.actorCase = actorCase;
    }

    @Override
    public Case getCase() {
        return this.actorCase;
    }

    /**
     * todo javadoc
     * */
    public String getFullName() {
        String firstname = (String) actorCase.getDataSet().get(ActorConstants.FIRSTNAME_FIELD_ID).getRawValue();
        String lastname = (String) actorCase.getDataSet().get(ActorConstants.LASTNAME_FIELD_ID).getRawValue();
        return String.join(" ", firstname, lastname);
    }

    /**
     * todo javadoc
     * */
    public String getEmail() {
        return (String) actorCase.getDataSet().get(ActorConstants.EMAIL_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getFirstname() {
        return (String) actorCase.getDataSet().get(ActorConstants.FIRSTNAME_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getLastname() {
        return (String) actorCase.getDataSet().get(ActorConstants.LASTNAME_FIELD_ID).getRawValue();
    }
}
