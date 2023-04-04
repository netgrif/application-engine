package com.netgrif.application.engine.action

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class AssignRemoveTest extends EngineTest {

    private Authentication auth

    @BeforeEach
    void before() {
        truncateDbs()
        def user = userService.system

        auth = new UsernamePasswordAuthenticationToken(user.transformToLoggedUser(), user)
        SecurityContextHolder.getContext().setAuthentication(auth)
    }

    @Test
    @Disabled("Create functions or update test")
    void testAssignAndRemoveRole() throws MissingPetriNetMetaDataException, IOException {
        ImportPetriNetEventOutcome netOptional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/role_assign_remove_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert netOptional.getNet() != null
        def net = netOptional.getNet()
        def roleCount = userService.system.processRoles.size()

        // create
        Case caze = workflowService.createCase(net.stringId, 'TEST', '', userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
        assert userService.system.processRoles.size() == roleCount + 4

        // delete
        workflowService.deleteCase(caze.stringId)
        assert userService.system.processRoles.size() == roleCount
    }
}
