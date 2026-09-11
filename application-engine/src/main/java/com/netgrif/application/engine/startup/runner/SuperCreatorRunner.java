package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.constants.UserConstants;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.netgrif.application.engine.objects.auth.domain.Authority.admin;

@Slf4j
@Component
@RunnerOrder(150)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "netgrif.engine.security.auth.create-super", matchIfMissing = true)
public class SuperCreatorRunner implements ApplicationEngineStartupRunner {

    private static final int GENERATED_PASSWORD_LENGTH = 27;

    private final SecurityConfigurationProperties securityProperties;
    private final AuthorityService authorityService;
    private final UserService userService;
    private final GroupService groupService;
    private final ProcessRoleService processRoleService;

    @Getter
    private AbstractUser superUser;

    @Override
    public void run(ApplicationArguments strings) {
        log.info("Creating Super user");
        createSuperUser();
    }

    private AbstractUser createSuperUser() {
        SecurityConfigurationProperties.AuthProperties authProperties = securityProperties.getAuth();
        Optional<AbstractUser> existingUser = userService.findUserByUsername(authProperties.getUsername(), null);

        if (existingUser.isPresent()) {
            log.info("Super user detected");
            this.superUser = existingUser.get();
            return this.superUser;
        }

        AbstractUser user = userService.createUser(
                authProperties.getUsername(),
                authProperties.getEmail(),
                UserConstants.ADMIN_USER_FIRST_NAME,
                UserConstants.ADMIN_USER_LAST_NAME,
                resolveAdminPassword(authProperties),
                null
        );
        user.setAuthoritySet(createSuperUserAuthorities());
        user.setProcessRoles(new HashSet<>(processRoleService.findAll(Pageable.unpaged()).getContent()));

        this.superUser = userService.saveUser(user, null);
        log.info("Super user created");
        log.info("Login: {}", authProperties.getUsername());
        return this.superUser;
    }

    String resolveAdminPassword(SecurityConfigurationProperties.AuthProperties authProperties) {
        if (authProperties.getAdminPassword() != null && !authProperties.getAdminPassword().isBlank()) {
            return authProperties.getAdminPassword();
        }

        String generatedPassword = RandomStringUtils.secure().nextAlphanumeric(GENERATED_PASSWORD_LENGTH);
        log.warn("!IMPORTANT!");
        log.warn("------------------------------------------------");
        log.warn("No password configured for super user. A random password was generated.");
        log.warn("Login: {}", authProperties.getUsername());
        log.warn("Password: {}", generatedPassword);
        log.warn("------------------------------------------------");
        return generatedPassword;
    }

    private Set<Authority> createSuperUserAuthorities() {
        return Set.of(
                authorityService.getOrCreate(admin),
                authorityService.getOrCreate(Authority.systemAdmin)
        );
    }

    public void setAllToSuperUser() {
        setAllGroups();
        setAllProcessRoles();
        setAllAuthorities();
        log.info("Super user updated");
    }

    public void setAllGroups() {
        groupService.findAll(Pageable.unpaged()).forEach(g -> groupService.addUser(g, getSuperUser()));
    }

    public void setAllProcessRoles() {
        superUser.setProcessRoles(new HashSet<>(processRoleService.findAll(Pageable.unpaged()).getContent()));
        superUser = userService.saveUser(superUser, null);
    }

    public void setAllAuthorities() {
        superUser.setAuthoritySet(new HashSet<>(authorityService.findAll(Pageable.unpaged()).stream().toList()));
        superUser = userService.saveUser(superUser, null);
    }

    public LoggedUser getLoggedSuper() {
        return ActorTransformer.toLoggedUser(superUser);
    }

}
