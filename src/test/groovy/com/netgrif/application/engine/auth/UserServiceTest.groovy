package com.netgrif.application.engine.auth

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static com.netgrif.application.engine.petrinet.domain.VersionType.MAJOR

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class UserServiceTest extends EngineTest {

    @Test
    @Disabled("Create functions or update test")
    void removeRole() {
        truncateDbs()

        Optional<PetriNet> netOptional = importHelper.createNet("role_test.xml", MAJOR, userService.system.transformToLoggedUser())
        assert netOptional.get() != null

        def net = netOptional.get()
        def roles = processRoleService.findAll(net.stringId)
        def roleCount = userService.system.processRoles.size()
        roles.each {
            userService.addRole(userService.system, it.stringId)
        }
        assert userService.system.processRoles.size() == roleCount + 3

        roles.each {
            userService.removeRole(userService.system, it.stringId)
        }
        assert userService.system.processRoles.size() == roleCount
    }
}