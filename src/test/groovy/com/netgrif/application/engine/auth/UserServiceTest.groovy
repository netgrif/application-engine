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

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class UserServiceTest {

    @Autowired
    private EngineTest helper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IUserService service

    @Autowired
    private IProcessRoleService roleService

    @Test
    @Disabled("Create functions or update test")
    void removeRole() {
        helper.truncateDbs()

        Optional<PetriNet> netOptional = importHelper.createNet("role_test.xml", VersionType.MAJOR, service.system.transformToLoggedUser())
        assert netOptional.get() != null

        def net = netOptional.get()
        def roles = roleService.findAll(net.stringId)
        def roleCount = service.system.processRoles.size()
        roles.each {
            service.addRole(service.system, it.stringId)
        }
        assert service.system.processRoles.size() == roleCount + 3

        roles.each {
            service.removeRole(service.system, it.stringId)
        }
        assert service.system.processRoles.size() == roleCount
    }
}