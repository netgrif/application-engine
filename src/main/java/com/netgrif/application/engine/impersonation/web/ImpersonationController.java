package com.netgrif.application.engine.impersonation.web;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserResourceHelperService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.application.engine.impersonation.exceptions.IllegalImpersonationAttemptException;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.impersonation.web.requestbodies.SearchRequest;
import com.netgrif.application.engine.workflow.web.responsebodies.ResourceLinkAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.inject.Provider;
import java.util.Locale;

@RestController
@RequestMapping("/api/impersonate")
@ConditionalOnProperty(
        value = "nae.impersonation.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Impersonation")
public class ImpersonationController {

    @Autowired
    protected IImpersonationService impersonationService;

    @Autowired
    protected IImpersonationAuthorizationService impersonationAuthorizationService;

    @Autowired
    protected IUserService userService;

    @Autowired
    protected IUserResourceHelperService userResourceHelperService;

    @Autowired
    protected Provider<UserResourceAssembler> userResourceAssemblerProvider;

    protected UserResourceAssembler getUserResourceAssembler(Locale locale, boolean small, String selfRel) {
        UserResourceAssembler result = userResourceAssemblerProvider.get();
        result.initialize(locale, small, selfRel);
        return result;
    }

    @Operation(summary = "Search impersonable users", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<UserResource> getImpersonationUserOptions(@RequestBody SearchRequest request, Pageable pageable, PagedResourcesAssembler<IUser> assembler, Authentication auth, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Page<IUser> page = impersonationAuthorizationService.getConfiguredImpersonationUsers(request.getQuery(), loggedUser, pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ImpersonationController.class)
                .getImpersonationUserOptions(request, pageable, assembler, auth, locale)).withRel("all");
        PagedModel<UserResource> resources = assembler.toModel(page, getUserResourceAssembler(locale, false, "all"), selfLink);
        ResourceLinkAssembler.addLinks(resources, IUser.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Impersonate user through a specific configuration", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping("/config/{id}")
    public UserResource impersonateByConfig(@PathVariable("id") String configId, Locale locale) throws IllegalImpersonationAttemptException, ImpersonatedUserHasSessionException {
        LoggedUser loggedUser = userService.getLoggedUser().transformToLoggedUser();
        if (!impersonationAuthorizationService.canImpersonate(loggedUser, configId)) {
            throw new IllegalImpersonationAttemptException(loggedUser, configId);
        }
        loggedUser = impersonationService.impersonateByConfig(configId);
        return userResourceHelperService.getResource(loggedUser, locale, false);
    }

    @Operation(summary = "Impersonate user directly by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping("/user/{id}")
    public UserResource impersonateUser(@PathVariable("id") String userId, Locale locale) throws IllegalImpersonationAttemptException, ImpersonatedUserHasSessionException {
        LoggedUser loggedUser = userService.getLoggedUser().transformToLoggedUser();
        if (!impersonationAuthorizationService.canImpersonateUser(loggedUser, userId)) {
            throw new IllegalImpersonationAttemptException(loggedUser, userId);
        }
        loggedUser = impersonationService.impersonateUser(userId);
        return userResourceHelperService.getResource(loggedUser, locale, false);
    }

    @Operation(summary = "Stop impersonating currently impersonated user", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping("/clear")
    public UserResource endImpersonation(Locale locale) {
        LoggedUser loggedUser = impersonationService.endImpersonation();
        return userResourceHelperService.getResource(loggedUser, locale, false);
    }

}
