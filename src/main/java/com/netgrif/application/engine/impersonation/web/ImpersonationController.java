package com.netgrif.application.engine.impersonation.web;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.responsebodies.IUserFactory;
import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.application.engine.impersonation.exceptions.IllegalImpersonationAttemptException;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.impersonation.web.requestBodies.SearchRequest;
import com.netgrif.application.engine.workflow.web.responsebodies.ResourceLinkAssembler;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ImpersonationController {

    @Autowired
    private IImpersonationService impersonationService;

    @Autowired
    private IImpersonationAuthorizationService impersonationAuthorizationService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserFactory userFactory;

    @Autowired
    private Provider<UserResourceAssembler> userResourceAssemblerProvider;

    protected UserResourceAssembler getUserResourceAssembler(Locale locale, boolean small, String selfRel) {
        UserResourceAssembler result = userResourceAssemblerProvider.get();
        result.initialize(locale, small, selfRel);
        return result;
    }

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

    @PostMapping("/{id}")
    public UserResource impersonate(@PathVariable("id") String userId, Locale locale) throws IllegalImpersonationAttemptException, ImpersonatedUserHasSessionException {
        LoggedUser loggedUser = userService.getLoggedUser().transformToLoggedUser();
        if (!loggedUser.isAdmin() && !impersonationAuthorizationService.canImpersonate(loggedUser, userId)) {
            throw new IllegalImpersonationAttemptException(loggedUser, userId);
        }
        loggedUser = impersonationService.impersonate(userId);
        return resource(loggedUser, locale, false);
    }

    @PostMapping("/clear")
    public UserResource endImpersonation(Locale locale) {
        LoggedUser loggedUser = impersonationService.endImpersonation();
        return resource(loggedUser, locale, false);
    }


    public UserResource resource(LoggedUser loggedUser, Locale locale, boolean small) {
        IUser user = userService.resolveById(loggedUser.getId(), small);
        User localisedUser = loggedUser.isImpersonating() ?
                localisedUser(user, userService.resolveById(loggedUser.getImpersonated().getId(), small), locale) :
                localisedUser(user, locale);
        return new UserResource(localisedUser, "profile");
    }

    public User localisedUser(IUser user, IUser impersonated, Locale locale) {
        User localisedUser = localisedUser(user, locale);
        User impersonatedUser = userFactory.getUser(impersonated, locale);
        localisedUser.setImpersonated(impersonatedUser);
        return localisedUser;
    }

    public User localisedUser(IUser user, Locale locale) {
        return userFactory.getUser(user, locale);
    }
}
