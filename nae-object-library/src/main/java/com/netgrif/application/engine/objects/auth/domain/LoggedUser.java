package com.netgrif.application.engine.objects.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Duration;
import java.util.Set;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class LoggedUser extends AbstractUser implements Serializable {

    private String workspaceId;
    private String providerOrigin;
    private Set<String> mfaMethods;
    private transient Duration sessionTimeout = Duration.ofMinutes(30);
}
