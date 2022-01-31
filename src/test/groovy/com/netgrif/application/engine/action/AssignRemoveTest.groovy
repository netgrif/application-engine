package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
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
    private SuperCreator superCreator;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private IUserService userService

    private static final String USER_EMAIL = "test@test.com"

    private Authentication auth

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        def user = userService.system;

        auth = new UsernamePasswordAuthenticationToken(user.transformToLoggedUser(), user)
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @Disabled("Create functions or update test")
    public void testAssignAndRemoveRole() throws MissingPetriNetMetaDataException, IOException {
        ImportPetriNetEventOutcome netOptional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/role_assign_remove_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());

        assert netOptional.getNet() != null;
        def net = netOptional.getNet()
        def roleCount = userService.system.userProcessRoles.size()

        // create
        Case caze = workflowService.createCase(net.stringId, 'TEST', '', userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
        assert userService.system.userProcessRoles.size() == roleCount + 4

        // delete
        workflowService.deleteCase(caze.stringId)
        assert userService.system.userProcessRoles.size() == roleCount
    }
}
