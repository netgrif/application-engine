package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class SuperCreator extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(SuperCreator.class.name)

    @Autowired
    private OrganizationRepository organizationRepository

    @Autowired
    private AuthorityRepository authorityRepository

    @Autowired
    private IUserProcessRoleService userProcessRoleService

    @Autowired
    private IUserService userService

    private User superUser

    @Override
    void run(String... strings) {
        log.info("Creating Super user")
        createSuperUser()
    }

    private User createSuperUser() {
        Authority adminAuthority = authorityRepository.findByName(Authority.admin)
        if (adminAuthority == null)
            adminAuthority = authorityRepository.save(new Authority(Authority.admin)) as Authority

        this.superUser = userService.saveNew(new User(
                name: "Super",
                surname: "Trooper",
                email: "super@netgrif.com",
                password: "password",
                authorities: [adminAuthority] as Set<Authority>,
                organizations: organizationRepository.findAll() as Set<Organization>,
                userProcessRoles: userProcessRoleService.findAllMinusDefault() as Set<UserProcessRole>))

        log.info("Super user created")
        return superUser
    }

    public void setAllToSuperUser() {
        superUser.setOrganizations(organizationRepository.findAll() as Set<Organization>)
        superUser.setUserProcessRoles(userProcessRoleService.findAllMinusDefault() as Set<UserProcessRole>)

        superUser = userService.save(superUser)
        log.info("Super user updated")
    }

    public User getSuperUser() {
        return superUser
    }
}