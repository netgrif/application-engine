package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.TenantService;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.constants.UserConstants;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.tenant.Tenant;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.netgrif.application.engine.objects.tenant.TenantConstants.ADMIN_TENANT_ID;
import static com.netgrif.application.engine.objects.tenant.TenantConstants.TENANT_ID;


@Slf4j
@Component
@RunnerOrder(150)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "netgrif.engine.security.auth.create-super", matchIfMissing = true)
public class SuperCreatorRunner implements ApplicationEngineStartupRunner {

    public static final String SUPER_ADMIN_EMAIL = "super@netgrif.com";
    private final SecurityConfigurationProperties securityProperties;
    private final AuthorityService authorityService;
    private final UserService userService;
    private final GroupService groupService;
    private final ProcessRoleService processRoleService;
    private final TenantService tenantService;

    @Getter
    private AbstractUser superUser;

    @Override
    public void run(ApplicationArguments strings) {
        log.info("Creating Super user");
        createSuperUser();
    }

    private AbstractUser createSuperUser() {
        Authority adminAuthority = authorityService.getOrCreate(Authority.admin);
        Authority systemAuthority = authorityService.getOrCreate(Authority.systemAdmin);
        Set<Authority> authorities = new HashSet<>();
        authorities.add(adminAuthority);
        authorities.add(systemAuthority);
        Optional<Tenant> adminTenant = tenantService.getById(ADMIN_TENANT_ID);
        if (adminTenant.isEmpty()) {
            throw new IllegalStateException("Admin tenant not found");
        }
        Optional<String> defaultRealmId = adminTenant.get().getDefaultRealmId();
        if (defaultRealmId.isEmpty()) {
            throw new IllegalStateException("Default realm not found");
        }
        Optional<AbstractUser> superUser = userService.findUserByUsername(UserConstants.ADMIN_USER_USERNAME, defaultRealmId.get());
        if (superUser.isEmpty()) {
            User user = new User();
            user.setFirstName(UserConstants.ADMIN_USER_FIRST_NAME);
            user.setLastName(UserConstants.ADMIN_USER_LAST_NAME);
            user.setUsername(UserConstants.ADMIN_USER_USERNAME);
            user.setEmail(UserConstants.ADMIN_USER_EMAIL);
            PasswordCredential passwordCredential = new PasswordCredential(securityProperties.getAuth().getAdminPassword(), 0, true);
            user.setCredential("password", passwordCredential);
            user.setState(UserState.ACTIVE);
            user.setAuthoritySet(authorities);
            user.setProcessRoles(new HashSet<>(processRoleService.findAll(Pageable.unpaged()).getContent()));
            user.setRealmId(defaultRealmId.get());
            user.setAttribute(TENANT_ID, ADMIN_TENANT_ID, false);
            this.superUser = userService.createUser(user, defaultRealmId.get());
            log.info("Super user created");
        } else {
            log.info("Super user detected");
            this.superUser = superUser.get();
        }

        return this.superUser;
    }

    public void setAllToSuperUser() {
        setAllGroups();
        setAllProcessRoles();
        setAllAuthorities();
        log.info("Super user updated");
    }

    public void setAllGroups() {
        groupService.findAll(Pageable.unpaged()).forEach(g -> groupService.addUser(getSuperUser(), g));
    }

    public void setAllProcessRoles() {
        superUser.setProcessRoles(new HashSet<>(processRoleService.findAll(Pageable.unpaged()).getContent()));
        superUser = userService.saveUser(superUser, superUser.getRealmId());
    }

    public void setAllAuthorities() {
        superUser.setAuthoritySet(new HashSet<>(authorityService.findAll(Pageable.unpaged()).stream().toList()));
        superUser = userService.saveUser(superUser, superUser.getRealmId());
    }

    public LoggedUser getLoggedSuper() {
        return ActorTransformer.toLoggedUser(superUser);
    }

}
