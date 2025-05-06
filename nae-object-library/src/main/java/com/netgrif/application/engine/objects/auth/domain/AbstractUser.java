package com.netgrif.application.engine.objects.auth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    public String getName() {
        return String.join(" ", firstName,
                middleName != null && !middleName.isEmpty() ? middleName : "",
                lastName).trim();
    }
}
