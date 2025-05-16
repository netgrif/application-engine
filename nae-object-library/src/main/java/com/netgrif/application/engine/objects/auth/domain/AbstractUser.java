package com.netgrif.application.engine.objects.auth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractUser extends AbstractActor {

    @NotNull
    protected String username;

    @NotNull
    protected String firstName;

    protected String middleName;

    @NotNull
    protected String lastName;

    protected String email;

    protected String avatar;

    public abstract String getPassword();

    public abstract void setPassword(String password);

    public void setCredential(String key, Credential<?> credential) {}

    public void setCredential(String type, String value, int order, boolean enabled) {}

    public void activateMFA(String type, String secret) {}

    public boolean isCredentialEnabled(String type) {
        return false;
    }

    public Credential<?> getCredential(String type) {
        return null;
    }

    public void disableCredential(String type) {}

    public void setCredentialProperty(String type, String key, Object value) {}

    public Object getCredentialProperty(String type, String key) {
        return null;
    }

    @Override
    public String getName() {
        return String.join(" ", firstName,
                middleName != null && !middleName.isEmpty() ? middleName : "",
                lastName).trim();
    }
}
