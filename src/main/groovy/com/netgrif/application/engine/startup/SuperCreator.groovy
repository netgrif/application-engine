package com.netgrif.application.engine.startup


import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.LoggedIdentity
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

import java.util.stream.Collectors

@Slf4j
@Component
@CompileStatic
@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
class SuperCreator extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ApplicationRoleRunner applicationRoleRunner

    @Autowired
    private IIdentityService identityService

    @Autowired
    private INextGroupService groupService

    @Autowired
    private IRoleService roleService

    @Autowired
    private SuperAdminConfiguration configuration

    private Identity superIdentity

    @Override
    void run(String... strings) {
        log.info("Creating Super identity")
        createSuperUser()
    }

    private void createSuperUser() {
        Optional<Identity> superIdentityOpt = identityService.findByUsername(configuration.email)
        if (superIdentityOpt.isPresent()) {
            log.info("Super identity detected")
            this.superIdentity = superIdentityOpt.get()
            return
        }
        this.superIdentity = identityService.createWithDefaultActor(IdentityParams.with()
                .username(new TextField(configuration.email))
                .firstname(new TextField(configuration.name))
                .lastname(new TextField(configuration.surname))
                .password(new TextField(configuration.password))
                .build())

        Set<String> allRoleIds = roleService.findAll().stream().map { it.stringId }.collect(Collectors.toSet())
        Role adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE)
        Role systemAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.SYSTEM_ADMIN_APP_ROLE)
        allRoleIds.add(adminAppRole.getStringId())
        allRoleIds.add(systemAppRole.getStringId())
        roleService.assignRolesToActor(this.superIdentity.stringId, allRoleIds)

        log.info("Super identity created with actor")
    }

    void setAllToSuperUser() {
        setAllGroups()
        setAllRoles()
        log.info("Super identity updated")
    }

    void setAllGroups() {
        // todo 2058 groups
//        groupService.findAllGroups().each {
//            groupService.addUser(superIdentity, it)
//        }
    }

    void setAllRoles() {
        Set<String> allRoleIds = roleService.findAll().stream().map { it.stringId }.collect(Collectors.toSet())
        roleService.assignRolesToActor(this.superIdentity.stringId, allRoleIds)
    }

    Identity getSuperIdentity() {
        return this.superIdentity
    }

    LoggedIdentity getLoggedSuper() {
        return superIdentity.toSession()
    }
}
