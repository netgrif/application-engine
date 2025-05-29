package com.netgrif.application.engine.authentication.web;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.PublicStrategy;
import com.netgrif.application.engine.authentication.domain.throwable.WrongPublicConfigurationException;
import com.netgrif.application.engine.authentication.web.responsebodies.IdentityDTO;
import com.netgrif.application.engine.authentication.web.responsebodies.IdentityResource;
import com.netgrif.application.engine.configuration.PublicViewProperties;
import com.netgrif.application.engine.settings.domain.Preferences;
import com.netgrif.application.engine.settings.web.PreferencesResource;
import com.netgrif.application.engine.startup.AnonymousIdentityRunner;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "nae.public.identity.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Public Identity Controller")
@RequestMapping("/api/public/identity")
public class PublicIdentityController {

    private final IdentityController identityController;
    private final PublicViewProperties publicViewProperties;


    @Operation(summary = "Get identity's preferences")
    @GetMapping(value = "/preferences", produces = MediaTypes.HAL_JSON_VALUE)
    public PreferencesResource preferences() {
        return identityController.preferences();
    }

    @Operation(summary = "Set identity's preferences")
    @PostMapping(value = "/preferences", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource savePreferences(@RequestBody Preferences preferences) throws WrongPublicConfigurationException {
        if (publicViewProperties.getStrategy().equals(PublicStrategy.SIMPLE)) {
            throw new WrongPublicConfigurationException(String.format("Cannot save preferences with configured [%s] public strategy!",
                    PublicStrategy.SIMPLE.name()));
        }
        return identityController.savePreferences(preferences);
    }

    @Operation(summary = "Get logged anonymous identity", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/me", produces = MediaTypes.HAL_JSON_VALUE)
    public IdentityResource getLoggedIdentity() {
        LoggedIdentity loggedIdentity = (LoggedIdentity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (loggedIdentity.getProperties().containsKey(AnonymousIdentityRunner.getAnonymousFlag())) {
            return new IdentityResource(new IdentityDTO(loggedIdentity), "me");
        }
        return null;
    }
}
