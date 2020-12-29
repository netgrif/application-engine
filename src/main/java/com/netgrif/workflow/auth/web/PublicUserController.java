package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.responsebodies.IUserFactory;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import com.netgrif.workflow.workflow.web.PublicAbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class PublicUserController extends PublicAbstractController {

    @Autowired
    private IUserFactory userResponseFactory;

    public PublicUserController(IUserService userService) {
        super(userService);
    }

    @ApiOperation(value = "Get logged user")
    @GetMapping(value = "/me", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource getLoggedUser(Locale locale) {
        return new UserResource(userResponseFactory.getUser(getAnonym().transformToAnonymousUser(), locale), "profile");
    }
}
