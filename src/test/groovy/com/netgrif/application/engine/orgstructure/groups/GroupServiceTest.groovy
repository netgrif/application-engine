package com.netgrif.application.engine.orgstructure.groups

import com.netgrif.adapter.auth.domain.User
import com.netgrif.adapter.workflow.domain.QCase
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.GroupRunner
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.auth.service.GroupService
import com.netgrif.auth.service.UserService
import com.netgrif.core.auth.domain.Authority
import com.netgrif.core.auth.domain.Group
import com.netgrif.core.auth.domain.QGroup
import com.netgrif.core.auth.domain.enums.UserState
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.core.petrinet.domain.roles.ProcessRole
import com.netgrif.core.workflow.domain.Case
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class GroupServiceTest {

    public static final String DUMMY_USER_MAIL = "dummy@netgrif.com"
    public static final String CUSTOMER_USER_MAIL = "customer@netgrif.com"

    @Autowired
    GroupService groupService

    @Autowired
    GroupRunner groupRunner

    @Autowired
    UserService userService

    @Autowired
    private ImportHelper importHelper

    @Autowired
    TestHelper testHelper

    @Test
    void groupTest() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        importHelper.createUser(new User(firstName: "Dummy", lastName: "User", email: DUMMY_USER_MAIL, username: DUMMY_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
        importHelper.createUser(new User(firstName: "Customer", lastName: "User", email: CUSTOMER_USER_MAIL, username: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])

        Group customGroup = createGroup()
        if (customGroup == null) {
            throw new NullPointerException()
        }

        List<Group> allGroups = findAllGroups()
        assert !allGroups.isEmpty()

        List<Group> byPredicate = findGroup()
        assert !byPredicate.isEmpty()

        Group addedUserGroup = addUser()
        assert !addedUserGroup.getMemberIds().isEmpty()

        Group removedUserGroup = removeUser()
        assert !removedUserGroup.getMemberIds().isEmpty()
    }

    Group createGroup() {
        return groupService.create("CUSTOM_GROUP_1", "CUSTOM_GROUP_1", userService.findUserByUsername(DUMMY_USER_MAIL, null).get())
    }

    List<Group> findGroup() {
        QGroup qGroup = new QGroup("group")
        return groupService.findByPredicate(qGroup.ownerUsername.eq(DUMMY_USER_MAIL), new FullPageRequest()).getContent()
    }

    List<Group> findAllGroups() {
        return groupService.findAll() as List
    }

    Group addUser() {
        QGroup qGroup = new QGroup("group")
        Group group = groupService.findByPredicate(qGroup.identifier.eq("CUSTOM_GROUP_1"), new FullPageRequest()).getContent().get(0)
        groupService.addUser(userService.findUserByUsername(CUSTOMER_USER_MAIL, null).get(), group)
        groupService.addUser(userService.findUserByUsername("engine@netgrif.com", null).get(), group)
        return group
    }

    Group removeUser() {
        QGroup qGroup = new QGroup("group")
        Group group = groupService.findByPredicate(qGroup.identifier.eq("CUSTOM_GROUP_1"), new FullPageRequest()).getContent().get(0)
        groupService.removeUser(userService.findUserByUsername(CUSTOMER_USER_MAIL, null).get(), group)
        return group
    }
}
