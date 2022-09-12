package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.Authorizations;
import com.netgrif.application.engine.auth.domain.Authorize;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.web.requestbodies.NewAuthorityRequest;
import com.netgrif.application.engine.auth.web.responsebodies.AuthorityResource;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import com.netgrif.application.engine.workflow.web.responsebodies.ResponseMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
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

    @Authorizations(value = {
            @Authorize(authority = "AUTHORITY_DELETE")
    })
    @ApiOperation(value = "Delete authority", authorizations = @Authorization("BasicAuth"))
    @DeleteMapping(value = "/delete/{name}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource delete(@PathVariable String name) {
        try {
            authorityService.delete(name);
            log.info("Authority [" + name + "] has been deleted successfully.");
            return new MessageResource(ResponseMessage.createSuccessMessage("Authority [" + name + "] has been deleted successfully."));
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            log.error("Failed to delete authority [" + name  + "].", e);
            return new MessageResource(ResponseMessage.createErrorMessage("Failed to delete authority."));
        }
    }

    @Authorizations(value = {
            @Authorize(authority = "AUTHORITY_CREATE")
    })
    @ApiOperation(value = "Delete authority", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<Authority> create(@RequestBody NewAuthorityRequest request) {
        try {
            Authority authority = authorityService.getOrCreate(request.name);
            log.info("Authority [" + authority + "] has been created successfully.");
            return AuthorityResource.of(authority);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            log.error("Failed to create authority [" + request.name  + "].", e);
            return null;
        }
    }
}
