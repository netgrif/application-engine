package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.constants.SystemUserConstants
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.authorization.domain.User
import com.netgrif.application.engine.authorization.domain.params.UserParams
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.authorization.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class SystemUserRunner extends AbstractOrderedCommandLineRunner {

    @Lazy
    @Autowired
    private IUserService userService

    @Autowired
    private IRoleService roleService

    @Autowired
    private ApplicationRoleRunner applicationRoleRunner

    private User systemUser

    @Override
    void run(String... strings) throws Exception {
        Optional<User> systemOpt = userService.findByEmail(SystemUserConstants.EMAIL)
        if (systemOpt.isPresent()) {
            this.systemUser = systemOpt.get()
            return
        }
        this.systemUser = createSystemIdentityWithUser()
    }

    private User createSystemIdentityWithUser() {
        User systemUser = userService.create(UserParams.with()
                .email(new TextField(SystemUserConstants.EMAIL))
                .firstname(new TextField(SystemUserConstants.FIRSTNAME))
                .lastname(new TextField(SystemUserConstants.LASTNAME))
                .build())

        Role adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE)
        Role systemAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.SYSTEM_ADMIN_APP_ROLE)
        roleService.assignRolesToActor(systemUser.stringId, [adminAppRole.stringId, systemAppRole.stringId] as Set)

        userService.registerForbiddenKeywords(Set.of(systemUser.getEmail()))
        log.info("Created system user [{}] with id [{}].", systemUser.getEmail(), systemUser.stringId)
        return systemUser
    }

    User getSystemUser() {
        return systemUser;
    }
}
