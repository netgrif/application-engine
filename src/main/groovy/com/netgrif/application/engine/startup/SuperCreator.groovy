package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.domain.*
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
@Component
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

    @Value('${nae.admin.password}')
    private String superAdminPassword

    private IUser superUser

    @Override
    void run(String... strings) {
        log.info("Creating Super user")
        createSuperUser()
    }

    private IUser createSuperUser() {
        Authority adminAuthority = authorityService.getOrCreate(Authority.admin)
        Authority systemAuthority = authorityService.getOrCreate(Authority.systemAdmin)

        IUser superUser = userService.findByEmail("super@netgrif.com", false)
        if (superUser == null) {
            this.superUser = userService.saveNew(new User(
                    name: "Admin",
                    surname: "Netgrif",
                    email: "super@netgrif.com",
                    password: superAdminPassword,
                    state: UserState.ACTIVE,
                    authorities: [adminAuthority, systemAuthority] as Set<Authority>,
                    processRoles: processRoleService.findAll() as Set<ProcessRole>))
            log.info("Super user created")
        } else {
            log.info("Super user detected")
            this.superUser = superUser
        }

        return this.superUser
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
