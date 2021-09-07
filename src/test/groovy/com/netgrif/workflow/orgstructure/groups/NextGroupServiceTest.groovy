package com.netgrif.workflow.orgstructure.groups

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.auth.service.UserService
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.startup.GroupRunner
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.QCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class NextGroupServiceTest {

    public static final String DUMMY_USER_MAIL = "dummy@netgrif.com"
    public static final String CUSTOMER_USER_MAIL = "customer@netgrif.com"

    @Autowired
    INextGroupService nextGroupService

    @Autowired
    GroupRunner groupRunner

    @Autowired
    UserService userService

    @Autowired
    TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Test
    void groupTest() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        importHelper.createUser(new User(name: "Dummy", surname: "User", email: DUMMY_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
//                [] as Group[],
                [] as ProcessRole[])
        importHelper.createUser(new User(name: "Customer", surname: "User", email: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
//                [] as Group[],
                [] as ProcessRole[])

        Optional<PetriNet> groupNet = importGroup()
        assert groupNet.isPresent()

//        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
//
//        importHelper.createUser(new User(name: "Test", surname: "Dummy", email: "dummy@netgrif.com", password: "password", state: UserState.ACTIVE),
//                [auths.get("user")] as Authority[],
//                [] as ProcessRole[])
//
//        importHelper.createUser(new User(name: "Test", surname: "Dummy", email: "user@netgrif.com", password: "password", state: UserState.ACTIVE),
//                [auths.get("user")] as Authority[],
//                [] as ProcessRole[])

        Case customGroup = createGroup()
        if (customGroup == null) {
            throw new NullPointerException()
        }

        List<Case> allGroups = findAllGroups()
        assert !allGroups.isEmpty()

        List<Case> byPredicate = findGroup()
        assert !byPredicate.isEmpty()

        Case addedUserGroup = addUser()
        assert !addedUserGroup.getDataSet().get("members").getOptions().isEmpty()

        Case removedUserGroup = removeUser()
        assert !removedUserGroup.getDataSet().get("members").getOptions().isEmpty()
    }

    Optional<PetriNet> importGroup() {
        return groupRunner.createDefaultGroup()
    }

    Case createGroup() {
        return nextGroupService.createGroup("CUSTOM_GROUP_1", userService.findByEmail(DUMMY_USER_MAIL, false))
    }

    List<Case> findGroup() {
        QCase qCase = new QCase("case")
        return nextGroupService.findByPredicate(qCase.author.email.eq(DUMMY_USER_MAIL))
    }

    List<Case> findAllGroups() {
        return nextGroupService.findAllGroups()
    }

    Case addUser() {
        QCase qCase = new QCase("case")
        Case group = nextGroupService.findByPredicate(qCase.title.eq("CUSTOM_GROUP_1")).get(0)
        nextGroupService.addUser(userService.findByEmail(CUSTOMER_USER_MAIL, false), group)
        nextGroupService.addUser(userService.findByEmail("engine@netgrif.com", false), group)
        return group
    }

    Case removeUser() {
        QCase qCase = new QCase("case")
        Case group = nextGroupService.findByPredicate(qCase.title.eq("CUSTOM_GROUP_1")).get(0)
        nextGroupService.removeUser(userService.findByEmail(CUSTOMER_USER_MAIL, false), group)
        return group
    }
}
