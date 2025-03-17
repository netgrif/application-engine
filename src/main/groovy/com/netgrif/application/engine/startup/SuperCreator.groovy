package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.*
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService
import com.netgrif.application.engine.authentication.service.interfaces.IUserService
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

import java.util.stream.Collectors

@Slf4j
@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
@Component
@CompileStatic
class SuperCreator extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IUserService userService

    @Autowired
    private INextGroupService groupService

    @Autowired
    private IRoleService roleService

    @Autowired
    private SuperAdminConfiguration configuration

    private IUser superUser

    @Override
    void run(String... strings) {
        log.info("Creating Super user")
        createSuperUser()
    }

    private void createSuperUser() {
        Authority adminAuthority = authorityService.getOrCreate(Authority.admin)
        Authority systemAuthority = authorityService.getOrCreate(Authority.systemAdmin)

        IUser superUser = userService.findByEmail(configuration.email)
        if (superUser != null) {
            log.info("Super user detected")
            this.superUser = superUser
            return
        }
        this.superUser = userService.saveNew(new User(
                name: configuration.name,
                surname: configuration.surname,
                email: configuration.email,
                password: configuration.password,
                state: UserState.ACTIVE,
                authorities: [adminAuthority, systemAuthority] as Set<Authority>))

        Set<String> allRoleIds = roleService.findAll().stream().map { it.stringId }.collect(Collectors.toSet())
        roleService.assignRolesToUser(this.superUser.stringId, allRoleIds)
        log.info("Super user created")
    }

    void setAllToSuperUser() {
        setAllGroups()
        setAllRoles()
        setAllAuthorities()
        log.info("Super user updated")
    }

    void setAllGroups() {
        groupService.findAllGroups().each {
            groupService.addUser(superUser, it)
        }
    }

    void setAllRoles() {
        Set<String> allRoleIds = roleService.findAll().stream().map { it.stringId }.collect(Collectors.toSet())
        roleService.assignRolesToUser(this.superUser.stringId, allRoleIds)
    }

    void setAllAuthorities() {
        superUser.setAuthorities(authorityService.findAll() as Set<Authority>)
        superUser = userService.save(superUser) as IUser
    }

    IUser getSuperUser() {
        return superUser
    }

    Identity getLoggedSuper() {
        return superUser.transformToLoggedUser()
    }
}
