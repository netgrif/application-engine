package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
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


@Slf4j
@Component
@RunnerOrder(150)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
public class SuperCreatorRunner implements ApplicationEngineStartupRunner {

    public static final String SUPER_ADMIN_EMAIL = "super@netgrif.com";
    private final SecurityConfigurationProperties securityProperties;
    private final AuthorityService authorityService;
    private final UserService userService;
    private final GroupService groupService;
    private final ProcessRoleService processRoleService;

    @Getter
    private IUser superUser;

    @Override
    public void run(ApplicationArguments strings) {
        log.info("Creating Super user");
        createSuperUser();
    }

    private IUser createSuperUser() {
        Authority adminAuthority = authorityService.getOrCreate(Authority.admin);
        Authority systemAuthority = authorityService.getOrCreate(Authority.systemAdmin);
        Set<Authority> authorities = new HashSet<>();
        authorities.add(adminAuthority);
        authorities.add(systemAuthority);

        Optional<IUser> superUser = userService.findUserByUsername(SUPER_ADMIN_EMAIL, null);
        if (superUser.isEmpty()) {
            User user = new com.netgrif.application.engine.adapter.spring.auth.domain.User();
            user.setFirstName("Admin");
            user.setLastName("Netgrif");
            user.setUsername(SUPER_ADMIN_EMAIL);
            user.setEmail(SUPER_ADMIN_EMAIL);
            user.setPassword(securityProperties.getAuth().getAdminPassword());
            user.setState(UserState.ACTIVE);
            user.setAuthorities(authorities);
            user.setProcessRoles(new HashSet<>(processRoleService.findAll()));
            this.superUser = userService.createUser(user, null);
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
        superUser.setProcessRoles(Set.copyOf(processRoleService.findAll()));
        superUser = userService.saveUser(superUser, null);
    }

    public void setAllAuthorities() {
        superUser.setAuthorities(Set.copyOf(authorityService.findAll()));
        superUser = userService.saveUser(superUser, null);
    }

    public LoggedUser getLoggedSuper() {
        return userService.transformToLoggedUser(superUser);
    }

}
