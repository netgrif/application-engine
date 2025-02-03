package com.netgrif.application.engine.startup.runner;

import com.netgrif.adapter.auth.service.AuthorityService;
import com.netgrif.application.engine.importer.model.Option;
import com.netgrif.auth.service.AuthorityServiceImpl;
import com.netgrif.core.auth.domain.*;
import com.netgrif.adapter.auth.service.AuthorityService;
import com.netgrif.adapter.auth.service.UserService;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.core.auth.domain.enums.UserState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
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

    private final AuthorityService authorityService;
    private final UserService userService;
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

        Optional<IUser> superUser = userService.findUserByUsername(SUPER_ADMIN_EMAIL, null);
        if (superUser.isEmpty()) {
            User user = new User();
            user.setFirstName("Admin");
            user.setLastName("Netgrif");
            user.setEmail(SUPER_ADMIN_EMAIL);
            user.setPassword(superAdminPassword);
            user.setState(UserState.ACTIVE);
            user.setAuthorities(Set.of(adminAuthority, systemAuthority));
            //MODULARISATION: refactor to core process roles
//            user.setProcessRoles(Set.copyOf(processRoleService.findAll()));
            this.superUser = userService.saveUser(user, null);
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
        groupService.findAllGroups().forEach(caze -> groupService.addUser(getSuperUser(), caze));
    }

    public void setAllProcessRoles() {
        //MODULARISATION: refactor to core process roles
//        superUser.setProcessRoles(Set.copyOf(processRoleService.findAll()));
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
