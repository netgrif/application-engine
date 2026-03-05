package com.netgrif.application.engine.orgstructure.groups

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.auth.service.GroupService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.Group
import com.netgrif.application.engine.objects.auth.domain.QGroup
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.GroupRunner
import com.netgrif.application.engine.utils.FullPageRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.util.Pair
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
    ProcessRoleService processRoleService

    @Autowired
    ImportHelper importHelper

    @Autowired
    IPetriNetService petriNetService

    @Autowired
    TestHelper testHelper

    AbstractUser dummy, customer

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        dummy = importHelper.createUser(new User(firstName: "Dummy", lastName: "User", email: DUMMY_USER_MAIL, username: DUMMY_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
        customer = importHelper.createUser(new User(firstName: "Customer", lastName: "User", email: CUSTOMER_USER_MAIL, username: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])

    }

    @Test
    void createGroup() {
        groupService.create("CUSTOM_GROUP_1", "CUSTOM_GROUP_1", userService.findUserByUsername(DUMMY_USER_MAIL, null).get())
        Optional<Group> groupOpt = groupService.findByIdentifier("CUSTOM_GROUP_1")
        assert groupOpt.isPresent()

    }

    @Test
    void findGroup() {
        QGroup qGroup = new QGroup("group")
        Page<Group> groupPage = groupService.findByPredicate(qGroup.ownerUsername.eq(DUMMY_USER_MAIL), Pageable.ofSize(1))
        assert !groupPage.isEmpty()
    }

    @Test
    void addAndRemoveUser() {
        QGroup qGroup = new QGroup("group")
        Group group = groupService.findByPredicate(qGroup.identifier.eq(DUMMY_USER_MAIL), new FullPageRequest()).getContent().get(0)
        group = groupService.addUser(userService.findUserByUsername(CUSTOMER_USER_MAIL, null).get(), group)
        assert group.getMemberIds().size() == 2
        group = groupService.removeUser(userService.findUserByUsername(CUSTOMER_USER_MAIL, null).get(), group)
        assert group.getMemberIds().size() == 1
    }

    @Test
    void addAndRemoveRole() {
        ImportPetriNetEventOutcome netWithRoleOutcome = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/simple_role.xml"))
                .releaseType(VersionType.MAJOR)
                .author(userService.getSystem())
                .build())
        ProcessRole role = netWithRoleOutcome.getNet().getRoles().values().find { it.importId == "simple_role"}
        Group group = groupService.create("addAndRemoveRole", "Add and remove role test group", dummy)
        group = groupService.addRole(group.getStringId(), role.getStringId())
        assert group.getProcessRoles().any {it.getStringId() == role.getStringId()}
        group = groupService.removeRole(group.getStringId(), role.getStringId())
        assert !group.getProcessRoles().any() {it.getStringId() == role.getStringId()}
    }

    @Test
    void addAndRemoveSubgroup() {
        Group group = groupService.findByIdentifier(dummy.getUsername()).orElse(null)
        assert group != null
        Group subGroup = groupService.create("addAndRemoveSubgroup", "Add and remove role test group", dummy)

        Pair<Group, Group> groupPair = groupService.addSubgroup(group.getStringId(), subGroup.getStringId())
        assert groupPair.getFirst().getSubgroupIds().contains(subGroup.getStringId())
        assert groupPair.getSecond().getGroupIds().contains(group.getStringId())

        groupPair = groupService.removeSubgroup(group.getStringId(), subGroup.getStringId())
        assert !groupPair.getFirst().getSubgroupIds().contains(subGroup.getStringId())
        assert !groupPair.getSecond().getGroupIds().contains(group.getStringId())
    }
}
