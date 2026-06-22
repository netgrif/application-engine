package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.adapter.spring.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.params.CreateCaseParams
import com.netgrif.application.engine.workflow.params.DeleteCaseParams
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class AssignRemoveTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreatorRunner superCreator;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private UserService userService

    private static final String USER_EMAIL = "test@test.com"

    private Authentication auth

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        def user = userService.system;

        auth = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(user), user)
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testAssignAndRemoveRole() throws MissingPetriNetMetaDataException, IOException {
        ImportPetriNetEventOutcome netOptional = petriNetService.importPetriNet(new ImportPetriNetParams(new FileInputStream("src/test/resources/petriNets/role_assign_remove_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()));

        assert netOptional.getNet() != null;
        def net = netOptional.getNet()
        def userAuthorities = importHelper.createAuthorities(["user": Authority.user])
        def testUser = importHelper.createUser(new User(firstName: "Test", lastName: "Integration", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [userAuthorities.get("user")] as Authority[],
                [] as ProcessRole[])
        def loggedUser = ActorTransformer.toLoggedUser(testUser)
        auth = new UsernamePasswordAuthenticationToken(loggedUser, "password", loggedUser.authorities)
        SecurityContextHolder.getContext().setAuthentication(auth)

        Set<String> actionRoleIds = net.roles.values()
                .findAll { ["first", "second", "third", "fourth"].contains(it.importId) }
                .collect { it.stringId } as Set

        // create
        Case caze = workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title('TEST')
                .color('')
                .author(loggedUser)
                .build()).getCase()
        def updatedUser = userService.findByEmail(USER_EMAIL, null)
        assert actionRoleIds.every { roleId -> updatedUser.processRoles.any { it.stringId == roleId } }

        // delete
        workflowService.deleteCase(new DeleteCaseParams(caze.stringId))
        updatedUser = userService.findByEmail(USER_EMAIL, null)
        assert actionRoleIds.every { roleId -> !updatedUser.processRoles.any { it.stringId == roleId } }
    }
}
