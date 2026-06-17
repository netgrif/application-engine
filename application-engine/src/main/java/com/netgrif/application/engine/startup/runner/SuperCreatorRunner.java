package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.constants.UserConstants;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
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


@Slf4j
@Component
@RunnerOrder(150)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "netgrif.engine.security.auth.create-super", matchIfMissing = true)
public class SuperCreatorRunner implements ApplicationEngineStartupRunner {

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
        createDummyUser();
    }

    private AbstractUser createSuperUser() {
        Authority adminAuthority = authorityService.getOrCreate(Authority.admin);
        Authority systemAuthority = authorityService.getOrCreate(Authority.systemAdmin);
        Set<Authority> authorities = new HashSet<>();
        authorities.add(adminAuthority);
        authorities.add(systemAuthority);

        Optional<AbstractUser> superUser = userService.findUserByUsername(UserConstants.ADMIN_USER_USERNAME, null);
        if (superUser.isEmpty()) {
            User user = new com.netgrif.application.engine.adapter.spring.auth.domain.User();
            user.setFirstName(UserConstants.ADMIN_USER_FIRST_NAME);
            user.setLastName(UserConstants.ADMIN_USER_LAST_NAME);
            user.setUsername(UserConstants.ADMIN_USER_USERNAME); // TODO: set ADMIN_USER_EMAIL or ADMIN_USER_USERNAME ? IDK
            user.setEmail(UserConstants.ADMIN_USER_EMAIL);
            PasswordCredential passwordCredential = new PasswordCredential(securityProperties.getAuth().getAdminPassword(), 0, true);
            user.setCredential("password", passwordCredential);
            user.setState(UserState.ACTIVE);
            user.setAuthoritySet(authorities);
            user.setProcessRoles(new HashSet<>(processRoleService.findAll(Pageable.unpaged()).getContent()));
            this.superUser = userService.createUser(user, null);
            log.info("Super user created");
        } else {
            log.info("Super user detected");
            this.superUser = superUser.get();
        }

        return this.superUser;
    }

    private void createDummyUser() {
        Authority userAuthority = authorityService.getOrCreate(Authority.user);
        Set<Authority> authorities = new HashSet<>();
        authorities.add(userAuthority);

        Optional<AbstractUser> superUser = userService.findUserByUsername("dummy", null);
        if (superUser.isEmpty()) {
            User user = new com.netgrif.application.engine.adapter.spring.auth.domain.User();
            user.setFirstName("Dummy");
            user.setLastName("User");
            user.setUsername("dummy");
            user.setEmail("dummy@netgrif.com");
            PasswordCredential passwordCredential = new PasswordCredential(securityProperties.getAuth().getAdminPassword(), 0, true);
            user.setCredential("password", passwordCredential);
            user.setState(UserState.ACTIVE);
            user.setAuthoritySet(authorities);
            Set<ProcessRole> processRoles = new HashSet<>();
            processRoles.add(processRoleService.getDefaultRole());
            user.setProcessRoles(processRoles);
           userService.createUser(user, null);
            log.info("Dummy user created");
        }
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
