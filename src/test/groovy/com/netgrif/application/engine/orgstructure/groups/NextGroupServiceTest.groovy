package com.netgrif.application.engine.orgstructure.groups

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.startup.GroupRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
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
    private ImportHelper importHelper

    @Autowired
    TestHelper testHelper

    @Test
    void groupTest() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        importHelper.createUser(new User(name: "Dummy", surname: "User", email: DUMMY_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
        importHelper.createUser(new User(name: "Customer", surname: "User", email: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])

        Optional<PetriNet> groupNet = importGroup()
        assert groupNet.isPresent()


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
        return nextGroupService.createGroup("CUSTOM_GROUP_1", userService.findByEmail(DUMMY_USER_MAIL, false)).getCase()
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
