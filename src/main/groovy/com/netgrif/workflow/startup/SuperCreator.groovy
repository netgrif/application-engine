package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.*
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.orgstructure.domain.Member
import com.netgrif.workflow.orgstructure.service.IGroupService
import com.netgrif.workflow.orgstructure.service.IMemberService
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

    public static final String TEST_USER_EMAIL = "user@netgrif.com"
    public static final String TEST_USER_NAME = "Test"
    public static final String TEST_USER_SURNAME = "User"

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IUserProcessRoleService userProcessRoleService

    @Autowired
    private IUserService userService

    @Autowired
    private IMemberService memberService

    @Autowired
    private IGroupService groupService

    @Value('${admin.password}')
    private String superAdminPassword

    private User superUser

    private Member superMember

    @Override
    void run(String... strings) {
        log.info("Creating Super user")
        createSuperUser()
    }

    private User createSuperUser() {
        Authority adminAuthority = authorityService.getOrCreate(Authority.admin)
        Authority systemAuthority = authorityService.getOrCreate(Authority.systemAdmin)

        User superUser = userService.findByEmail("super@netgrif.com", false)
        if (superUser == null) {
            this.superUser = userService.saveNew(new User(
                    name: "Admin",
                    surname: "Netgrif",
                    email: "super@netgrif.com",
                    password: superAdminPassword,
                    state: UserState.ACTIVE,
                    authorities: [adminAuthority, systemAuthority] as Set<Authority>,
                    userProcessRoles: userProcessRoleService.findAll() as Set<UserProcessRole>))
            this.superMember = memberService.findByEmail(this.superUser.email)
            log.info("Super user created")
        } else {
            log.info("Super user detected")
            this.superUser = superUser
            this.superMember = memberService.findByEmail(this.superUser.email)
        }

        Authority userAuthority = authorityService.getOrCreate(Authority.user)
        adminAuthority = authorityService.getOrCreate(Authority.admin)
        User testUser = userService.findByEmail(TEST_USER_EMAIL, false)
        if (testUser == null) {
            userService.saveNew(new User(
                    name: TEST_USER_NAME,
                    surname: TEST_USER_SURNAME,
                    email: TEST_USER_EMAIL,
                    password: "password",
                    state: UserState.ACTIVE,
                    authorities: [userAuthority, adminAuthority] as Set<Authority>,
                    userProcessRoles: new HashSet<UserProcessRole>()))
            log.info("Test user created")
        } else {
            log.info("Test user detected")
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
        groupService.findAll().each {
            it.addMember(superMember)
        }
        memberService.save(superMember)
    }

    void setAllProcessRoles() {
        superUser.setUserProcessRoles(userProcessRoleService.findAll() as Set<UserProcessRole>)
        superUser = userService.save(superUser)
    }

    void setAllAuthorities() {
        superUser.setAuthorities(authorityService.findAll() as Set<Authority>)
        superUser = userService.save(superUser)
    }

    User getSuperUser() {
        return superUser
    }

    LoggedUser getLoggedSuper() {
        return superUser.transformToLoggedUser()
    }
}