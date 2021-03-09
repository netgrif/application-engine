package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.UserSearchRequestBody;
import com.netgrif.workflow.auth.web.responsebodies.IUserFactory;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import com.netgrif.workflow.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.workflow.workflow.web.responsebodies.ResourceLinkAssembler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.inject.Provider;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/public/user")
@ConditionalOnProperty(
        value = "nae.user.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"User"})
public class PublicUserController {

    @Autowired
    private IUserFactory userResponseFactory;

    @Autowired
    private Provider<UserResourceAssembler> userResourceAssemblerProvider;

    @Autowired
    private IUserService userService;

    public PublicUserController() {
    }

    protected UserResourceAssembler getUserResourceAssembler(Locale locale, boolean small, String selfRel) {
        UserResourceAssembler result = userResourceAssemblerProvider.get();
        result.initialize(locale, small, selfRel);
        return result;
    }

    @ApiOperation(value = "Get logged user")
    @GetMapping(value = "/me", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource getLoggedUser(Locale locale) {
        return new UserResource(userResponseFactory.getUser(userService.getAnonymousLogged().transformToAnonymousUser(), locale), "profile");
    }

    @ApiOperation(value = "Generic user search", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<UserResource> search(@RequestParam(value = "small", required = false) Boolean small, @RequestBody UserSearchRequestBody query, Pageable pageable, PagedResourcesAssembler<User> assembler, Locale locale) {
        small = small == null ? false : small;
        Page<User> page = userService.searchAllCoMembers(query.getFulltext(), query.getRoles(), query.getNegativeRoles(), userService.getAnonymousLogged(), small, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PublicUserController.class)
                .search(small, query, pageable, assembler, locale)).withRel("search");
        PagedResources<UserResource> resources = assembler.toResource(page, getUserResourceAssembler(locale, small, "search"), selfLink);
        ResourceLinkAssembler.addLinks(resources, User.class, selfLink.getRel());
        return resources;
    }
}
