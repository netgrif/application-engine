package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.objects.auth.constants.SearchConstants;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Value("${nae.admin.password}")
    private String superAdminPassword;

    private final AuthorityService authorityService;
    private final UserService userService;
    private final GroupService groupService;
    private final ProcessRoleService processRoleService;

    @Getter
    private AbstractUser superUser;

    @Autowired
    protected BCryptPasswordEncoder bCryptPasswordEncoder;

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

        Optional<AbstractUser> superUser = userService.findUserByUsername(SUPER_ADMIN_EMAIL, null);
        if (superUser.isEmpty()) {
            User user = new User();
            user.setFirstName("Admin");
            user.setLastName("Netgrif");
            user.setUsername(SUPER_ADMIN_EMAIL);
            user.setEmail(SUPER_ADMIN_EMAIL);
            PasswordCredential passwordCredential = new PasswordCredential(bCryptPasswordEncoder.encode(superAdminPassword), 0, true);
            user.setCredential("password", passwordCredential);
            user.setState(UserState.ACTIVE);
            user.setAuthoritySet(authorities);
            user.setProcessRoles(new HashSet<>(processRoleService.findAll(Pageable.ofSize(SearchConstants.MAX_PAGE_SIZE))));
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
        groupService.findAll(Pageable.ofSize(SearchConstants.MAX_PAGE_SIZE)).forEach(g -> groupService.addUser(getSuperUser(), g));
    }

    public void setAllProcessRoles() {
        superUser.setProcessRoles(Set.copyOf(processRoleService.findAll(Pageable.ofSize(SearchConstants.MAX_PAGE_SIZE))));
        superUser = userService.saveUser(superUser, null);
    }

    public void setAllAuthorities() {
        superUser.setAuthoritySet(Set.copyOf(authorityService.findAll()));
        superUser = userService.saveUser(superUser, null);
    }

    public LoggedUser getLoggedSuper() {
        return ActorTransformer.toLoggedUser(superUser);
    }

}
