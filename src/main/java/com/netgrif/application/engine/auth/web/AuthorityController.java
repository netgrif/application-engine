package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.web.requestbodies.NewAuthorityRequest;
import com.netgrif.application.engine.auth.web.responsebodies.AuthorityResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/authority")
@ConditionalOnProperty(
        value = "nae.user.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"Authority"})
public class AuthorityController {

    @Autowired
    private IAuthorityService authorityService;

    @ApiOperation(value = "Delete authority", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/delete/{name}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<Authority> delete(@PathVariable String name, Authentication auth, Locale locale) {
        Authority deletedAuthority = authorityService.delete(name);
        return AuthorityResource.of(deletedAuthority);
    }

    @ApiOperation(value = "Delete authority", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<Authority> create(@RequestBody NewAuthorityRequest request, Authentication auth, Locale locale) {
        Authority authority = authorityService.getOrCreate(request.name);
        return AuthorityResource.of(authority);
    }
}
