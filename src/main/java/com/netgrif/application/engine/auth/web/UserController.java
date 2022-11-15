package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.IUserResourceHelperService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.interfaces.IUserController;
import com.netgrif.application.engine.auth.web.responsebodies.IUserFactory;
import com.netgrif.application.engine.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.settings.service.IPreferencesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Provider;

@Slf4j
@RestController
@RequestMapping("/api/user")
@ConditionalOnProperty(
        value = "nae.user.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "User")
public class UserController implements IUserController {

    private final IUserService userService;

    private final IUserResourceHelperService userResourceHelperService;

    private final IProcessRoleService processRoleService;

    private final IAuthorityService authorityService;

    private final IPreferencesService preferencesService;

    private final ServerAuthProperties serverAuthProperties;

    private final IUserFactory userResponseFactory;

    private final Provider<UserResourceAssembler> userResourceAssemblerProvider;

    private final ISecurityContextService securityContextService;

    public UserController(@Autowired IUserService userService,
                          @Autowired IUserResourceHelperService userResourceHelperService,
                          @Autowired IProcessRoleService processRoleService,
                          @Autowired IAuthorityService authorityService,
                          @Autowired IPreferencesService preferencesService,
                          @Autowired ServerAuthProperties serverAuthProperties,
                          @Autowired IUserFactory userResponseFactory,
                          @Autowired Provider<UserResourceAssembler> userResourceAssemblerProvider,
                          @Autowired ISecurityContextService securityContextService) {
        this.userService = userService;
        this.userResourceHelperService = userResourceHelperService;
        this.processRoleService = processRoleService;
        this.authorityService = authorityService;
        this.preferencesService = preferencesService;
        this.serverAuthProperties = serverAuthProperties;
        this.userResponseFactory = userResponseFactory;
        this.userResourceAssemblerProvider = userResourceAssemblerProvider;
        this.securityContextService = securityContextService;
    }

    @Override
    public IUserResourceHelperService userResourceHelperService() {
        return userResourceHelperService;
    }

    @Override
    public IProcessRoleService processRoleService() {
        return processRoleService;
    }

    @Override
    public IAuthorityService authorityService() {
        return authorityService;
    }

    @Override
    public IPreferencesService preferencesService() {
        return preferencesService;
    }

    @Override
    public ServerAuthProperties serverAuthProperties() {
        return serverAuthProperties;
    }

    @Override
    public IUserFactory userResponseFactory() {
        return userResponseFactory;
    }

    @Override
    public Provider<UserResourceAssembler> userResourceAssemblerProvider() {
        return userResourceAssemblerProvider;
    }

    @Override
    public ISecurityContextService securityContextService() {
        return securityContextService;
    }

    @Override
    public IUserService userService() {
        return userService;
    }

    @Override
    public Logger log() {
        return log;
    }
}
