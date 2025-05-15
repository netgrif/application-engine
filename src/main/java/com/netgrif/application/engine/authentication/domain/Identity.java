package com.netgrif.application.engine.authentication.domain;

import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Identity extends SystemCase {

    public Identity(Case identityCase) {
        super(identityCase);
    }

    @Override
    protected CanInitializeOutcome canInitialize(Case identityCase) {
        return new CanInitializeOutcome("Provided identity case is of different process",
                identityCase.getProcessIdentifier().equals(IdentityConstants.PROCESS_IDENTIFIER));
    }

    /**
     * todo javadoc
     * */
    public boolean isActive() {
        String state = (String) getCase().getDataSet().get(IdentityConstants.STATE_FIELD_ID).getRawValue();
        return state.equalsIgnoreCase(IdentityState.ACTIVE.name());
    }

    /**
     * todo javadoc
     * */
    public String getFirstname() {
        return (String) getCase().getDataSet().get(IdentityConstants.FIRSTNAME_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getLastname() {
        return (String) getCase().getDataSet().get(IdentityConstants.LASTNAME_FIELD_ID).getRawValue();
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
        List<String> mainActorIdAsList = ((CaseField) getCase().getDataSet().get(IdentityConstants.MAIN_ACTOR_FIELD_ID)).getRawValue();
        if (mainActorIdAsList != null && !mainActorIdAsList.isEmpty()) {
            return mainActorIdAsList.get(0);
        }
        return null;
    }

    /**
     * todo javadoc
     * */
    public List<String> getAdditionalActorIds() {
        return ((CaseField) getCase().getDataSet().get(IdentityConstants.ADDITIONAL_ACTORS_FIELD_ID)).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public Set<String> getAllActors() {
        List<String> additionalActorIds = getAdditionalActorIds();
        Set<String> allActorIds;
        if (additionalActorIds == null) {
            allActorIds = new HashSet<>();
        } else {
            allActorIds = new HashSet<>(getAdditionalActorIds());
        }

        String mainActorId = getMainActorId();
        if (mainActorId == null) {
            return allActorIds;
        }

        allActorIds.add(mainActorId);
        return allActorIds;
    }

    /**
     * todo javadoc
     * */
    public String getRegistrationToken() {
        return (String) getCase().getDataSet().get(IdentityConstants.REGISTRATION_TOKEN_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public LocalDateTime getExpirationDate() {
        return (LocalDateTime) getCase().getDataSet().get(IdentityConstants.EXPIRATION_DATE_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getUsername() {
        return (String) getCase().getDataSet().get(IdentityConstants.USERNAME_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getPassword() {
        return (String) getCase().getDataSet().get(IdentityConstants.PASSWORD_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public LoggedIdentity toSession() {
        String username = (String) getCase().getDataSet().get(IdentityConstants.USERNAME_FIELD_ID).getRawValue();

        return LoggedIdentity.with()
                .username(username)
                .password(this.getPassword())
                .fullName(this.getFullName())
                .identityId(this.getStringId())
                .activeActorId(this.getMainActorId())
                .build();
    }
}