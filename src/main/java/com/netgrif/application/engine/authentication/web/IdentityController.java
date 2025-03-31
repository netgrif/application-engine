package com.netgrif.application.engine.authentication.web;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authentication.web.responsebodies.IdentityDTO;
import com.netgrif.application.engine.authentication.web.responsebodies.IdentityResource;
import com.netgrif.application.engine.authorization.service.interfaces.IApplicationAuthorizationService;
import com.netgrif.application.engine.settings.domain.Preferences;
import com.netgrif.application.engine.settings.service.IPreferencesService;
import com.netgrif.application.engine.settings.web.PreferencesResource;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/identity")
@ConditionalOnProperty(
        value = "nae.identity.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Identity")
public class IdentityController {

    private final IIdentityService identityService;
    private final IApplicationAuthorizationService authorizationService;
    private final IPreferencesService preferencesService;

    // todo 2058
    // TODO: release/8.0.0 any more endpoints?

    @Operation(summary = "Get identity by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public IdentityResource getIdentity(@PathVariable("id") String identityId) {
        LoggedIdentity loggedIdentity = identityService.getLoggedIdentity();
        if (!Objects.equals(loggedIdentity.getIdentityId(), identityId) && authorizationService.hasApplicationRole("admin")) {
            log.info("Identity [{}] is trying to get different identity with ID [{}]", loggedIdentity.getUsername(), identityId);
            throw new IllegalArgumentException(String.format("Could not find identity with id [%s]", identityId));
        }
        Optional<Identity> identityOpt = identityService.findById(identityId);
        if (identityOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not find identity with id [%s]", identityId));
        }
        return new IdentityResource(new IdentityDTO(identityOpt.get(), loggedIdentity.getActiveActorId()), "profile");
    }

    @Operation(summary = "Get logged identity", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/me", produces = MediaTypes.HAL_JSON_VALUE)
    public IdentityResource getLoggedUser(Authentication auth) {
        LoggedIdentity loggedIdentity = (LoggedIdentity) auth.getPrincipal();
        return new IdentityResource(new IdentityDTO(loggedIdentity), "me");
    }

    @Operation(summary = "Get identity's preferences", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/preferences", produces = MediaTypes.HAL_JSON_VALUE)
    public PreferencesResource preferences() {
        LoggedIdentity loggedIdentity = identityService.getLoggedIdentity();
        Optional<Preferences> preferencesOpt = preferencesService.get(loggedIdentity.getIdentityId());

        if (preferencesOpt.isEmpty()) {
            preferencesOpt = Optional.of(new Preferences(loggedIdentity.getIdentityId()));
        }

        return new PreferencesResource(preferencesOpt.get());
    }

    @Operation(summary = "Set identity's preferences", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/preferences", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource savePreferences(@RequestBody Preferences preferences) {
        LoggedIdentity loggedIdentity = identityService.getLoggedIdentity();
        try {
            preferences.setIdentityId(loggedIdentity.getIdentityId());
            preferencesService.save(preferences);
            return MessageResource.successMessage("Identity's preferences saved");
        } catch (Exception e) {
            log.error("Saving identity's [{}] preferences failed with message: {}", loggedIdentity.getUsername(),
                    e.getMessage(), e);
            return MessageResource.errorMessage("Saving identity's preferences failed");
        }
    }
}
