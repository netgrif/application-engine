package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.domain.*;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;


@Slf4j
@Component
@RunnerOrder(150)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
public class SuperCreatorRunner implements ApplicationEngineStartupRunner {

    public static final String SUPER_ADMIN_EMAIL = "super@netgrif.com";

    @Value("${nae.admin.password}")
    private String superAdminPassword;

    private final IAuthorityService authorityService;
    private final IUserService userService;
    private final INextGroupService groupService;
    private final IProcessRoleService processRoleService;

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

        IUser superUser = userService.findByEmail(SUPER_ADMIN_EMAIL, false);
        if (superUser == null) {
            User user = new User();
            user.setName("Admin");
            user.setSurname("Netgrif");
            user.setEmail(SUPER_ADMIN_EMAIL);
            user.setPassword(superAdminPassword);
            user.setState(UserState.ACTIVE);
            user.setAuthorities(Set.of(adminAuthority, systemAuthority));
            user.setProcessRoles(Set.copyOf(processRoleService.findAll()));
            this.superUser = userService.saveNew(user);
            log.info("Super user created");
        } else {
            log.info("Super user detected");
            this.superUser = superUser;
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
        groupService.findAllGroups().forEach(caze -> groupService.addUser(getSuperUser(), caze));
    }

    public void setAllProcessRoles() {
        superUser.setProcessRoles(Set.copyOf(processRoleService.findAll()));
        superUser = userService.save(superUser);
    }

    public void setAllAuthorities() {
        superUser.setAuthorities(Set.copyOf(authorityService.findAll()));
        superUser = userService.save(superUser);
    }

    public LoggedUser getLoggedSuper() {
        return superUser.transformToLoggedUser();
    }

}