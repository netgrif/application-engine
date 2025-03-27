package com.netgrif.application.engine.authentication.domain;

import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.domain.Case;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Identity extends Case {

    /**
     * todo javadoc
     * */
    public boolean isActive() {
        String state = (String) getDataSet().get(IdentityConstants.STATE_FIELD_ID).getRawValue();
        return state.equalsIgnoreCase(IdentityState.ACTIVE.name());
    }

    /**
     * todo javadoc
     * */
    public String getFullName() {
        String firstname = (String) getDataSet().get(IdentityConstants.FIRSTNAME_FIELD_ID).getRawValue();
        String lastname = (String) getDataSet().get(IdentityConstants.LASTNAME_FIELD_ID).getRawValue();
        return String.join(" ", firstname, lastname);
    }

    /**
     * todo javadoc
     * */
    public String getMainActorId() {
        List<String> mainActorIdAsList = ((CaseField) getDataSet().get(IdentityConstants.MAIN_ACTOR_FIELD_ID)).getRawValue();
        if (!mainActorIdAsList.isEmpty()) {
            return mainActorIdAsList.get(0);
        }
        return null;
    }

    /**
     * todo javadoc
     * */
    public List<String> getAdditionalActorIds() {
        return ((CaseField) getDataSet().get(IdentityConstants.MAIN_ACTOR_FIELD_ID)).getRawValue();
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
        return (String) getDataSet().get(IdentityConstants.REGISTRATION_TOKEN_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public LocalDateTime getExpirationDate() {
        return (LocalDateTime) getDataSet().get(IdentityConstants.EXPIRATION_DATE_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getUsername() {
        return (String) getDataSet().get(IdentityConstants.USERNAME_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public String getPassword() {
        return (String) getDataSet().get(IdentityConstants.PASSWORD_FIELD_ID).getRawValue();
    }

    /**
     * todo javadoc
     * */
    public LoggedIdentity toSession() {
        return toSession(new HashSet<>());
    }

    /**
     * todo javadoc
     * */
    public LoggedIdentity toSession(Set<Authority> authorities) {
        String username = (String) getDataSet().get(IdentityConstants.USERNAME_FIELD_ID).getRawValue();

        if (authorities == null) {
            authorities = new HashSet<>();
        }

        return LoggedIdentity.builder()
                .username(username)
                .password(this.getPassword())
                .fullName(this.getFullName())
                .identityId(this.getStringId())
                .activeActorId(this.getMainActorId())
                .authorities(authorities)
                .build();
    }
}