package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.requestbodies.UserSearchRequestBody;
import com.netgrif.application.engine.auth.web.responsebodies.IUserFactory;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.application.engine.settings.domain.Preferences;
import com.netgrif.application.engine.settings.service.IPreferencesService;
import com.netgrif.application.engine.settings.web.PreferencesResource;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import com.netgrif.application.engine.workflow.web.responsebodies.ResourceLinkAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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
import org.springframework.web.bind.annotation.*;

import javax.inject.Provider;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestController
@ConditionalOnProperty(
        value = "nae.public.user.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Public User Controller")
@RequestMapping("/api/public/user")
public class PublicUserController {

    @Autowired
    private IUserFactory userResponseFactory;

    @Autowired
    private Provider<UserResourceAssembler> userResourceAssemblerProvider;

    @Autowired
    private IUserService userService;

    @Autowired
    private IPreferencesService preferencesService;

    public PublicUserController() {
    }

    protected UserResourceAssembler getUserResourceAssembler(Locale locale, boolean small, String selfRel) {
        UserResourceAssembler result = userResourceAssemblerProvider.get();
        result.initialize(locale, small, selfRel);
        return result;
    }

    @Operation(summary = "Get logged user")
    @GetMapping(value = "/me", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource getLoggedUser(Locale locale) {
        return new UserResource(userResponseFactory.getUser(userService.getAnonymousLogged().transformToAnonymousUser(), locale), "profile");
    }

    @Operation(summary = "Generic user search")
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<UserResource> search(@RequestParam(value = "small", required = false) Boolean small, @RequestBody UserSearchRequestBody query, Pageable pageable, PagedResourcesAssembler<IUser> assembler, Locale locale) {
        small = small == null ? false : small;
        Page<IUser> page = userService.searchAllCoMembers(query.getFulltext(),
                query.getRoles().stream().map(ObjectId::new).collect(Collectors.toList()),
                query.getNegativeRoles().stream().map(ObjectId::new).collect(Collectors.toList()),
                userService.getAnonymousLogged(), small, pageable);

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PublicUserController.class)
                .search(small, query, pageable, assembler, locale)).withRel("search");
        PagedModel<UserResource> resources = assembler.toModel(page, getUserResourceAssembler(locale, small, "search"), selfLink);
        ResourceLinkAssembler.addLinks(resources, IUser.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Get user's preferences")
    @GetMapping(value = "/preferences", produces = MediaTypes.HAL_JSON_VALUE)
    public PreferencesResource preferences() {
        String userId = userService.getAnonymousLogged().transformToAnonymousUser().getId();
        Preferences preferences = preferencesService.get(userId);

        if (preferences == null) {
            preferences = new Preferences(userId);
        }

        return new PreferencesResource(preferences);
    }

    @Operation(summary = "Set user's preferences")
    @PostMapping(value = "/preferences", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource savePreferences(@RequestBody Preferences preferences) {
        try {
            String userId = userService.getAnonymousLogged().transformToAnonymousUser().getId();
            preferences.setUserId(userId);
            preferencesService.save(preferences);
            return MessageResource.successMessage("User preferences saved");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MessageResource.errorMessage("Saving user preferences failed");
        }
    }
}
