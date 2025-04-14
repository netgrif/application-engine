package com.netgrif.application.engine.authentication.domain;

import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Identity implements SystemCase {

    private final Case identityCase;

    public Identity(Case identityCase) {
        this.identityCase = identityCase;
    }

    @Override
    public Case getCase() {
        return this.identityCase;
    }

    /**
     * todo javadoc
     * */
    public boolean isActive() {
        String state = (String) identityCase.getDataSet().get(IdentityConstants.STATE_FIELD_ID).getRawValue();
        return state.equalsIgnoreCase(IdentityState.ACTIVE.name());
    }

    /**
     * todo javadoc
     * */
    public String getFirstname() {
        return (String) identityCase.getDataSet().get(IdentityConstants.FIRSTNAME_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getLastname() {
        return (String) identityCase.getDataSet().get(IdentityConstants.LASTNAME_FIELD_ID).getRawValue();
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
    public String getMainActorId() {
        List<String> mainActorIdAsList = ((CaseField) identityCase.getDataSet().get(IdentityConstants.MAIN_ACTOR_FIELD_ID)).getRawValue();
        if (!mainActorIdAsList.isEmpty()) {
            return mainActorIdAsList.get(0);
        }
        return null;
    }

    /**
     * todo javadoc
     * */
    public List<String> getAdditionalActorIds() {
        return ((CaseField) identityCase.getDataSet().get(IdentityConstants.MAIN_ACTOR_FIELD_ID)).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public Set<String> getAllActors() {
        String mainActorId = getMainActorId();

        Set<String> allActorIds = new HashSet<>(getAdditionalActorIds());
        allActorIds.add(mainActorId);

        return allActorIds;
    }

    /**
     * todo javadoc
     * */
    public String getRegistrationToken() {
        return (String) identityCase.getDataSet().get(IdentityConstants.REGISTRATION_TOKEN_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public LocalDateTime getExpirationDate() {
        return (LocalDateTime) identityCase.getDataSet().get(IdentityConstants.EXPIRATION_DATE_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getUsername() {
        return (String) identityCase.getDataSet().get(IdentityConstants.USERNAME_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getPassword() {
        return (String) identityCase.getDataSet().get(IdentityConstants.PASSWORD_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public LoggedIdentity toSession() {
        String username = (String) identityCase.getDataSet().get(IdentityConstants.USERNAME_FIELD_ID).getRawValue();

        return LoggedIdentity.with()
                .username(username)
                .password(this.getPassword())
                .fullName(this.getFullName())
                .identityId(this.getStringId())
                .activeActorId(this.getMainActorId())
                .build();
    }
}