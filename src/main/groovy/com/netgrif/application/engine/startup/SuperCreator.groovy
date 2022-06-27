package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.domain.*
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.configuration.SuperAdminConfiguration
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
@Component
@CompileStatic
class SuperCreator extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperCreator.class.name)

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IUserService userService

    @Autowired
    private INextGroupService groupService

    @Autowired
    private IProcessRoleService processRoleService

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

        IUser superUser = userService.findByEmail(configuration.email, false)
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
                authorities: [adminAuthority, systemAuthority] as Set<Authority>,
                processRoles: processRoleService.findAll() as Set<ProcessRole>))
        log.info("Super user created")
    }

    void setAllToSuperUser() {
        setAllGroups()
        setAllProcessRoles()
        setAllAuthorities()
        log.info("Super user updated")
    }

    void setAllGroups() {
        groupService.findAllGroups().each {
            groupService.addUser(superUser, it)
        }
    }

    void setAllProcessRoles() {
        superUser.setProcessRoles(processRoleService.findAll() as Set<ProcessRole>)
        superUser = userService.save(superUser) as IUser
    }

    void setAllAuthorities() {
        superUser.setAuthorities(authorityService.findAll() as Set<Authority>)
        superUser = userService.save(superUser) as IUser
    }

    IUser getSuperUser() {
        return superUser
    }

    LoggedUser getLoggedSuper() {
        return superUser.transformToLoggedUser()
    }
}