package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
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
class UserServiceTest {

    @Autowired
    private TestHelper helper

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

        def netOptional = importHelper.createNet("role_test.xml", VersionType.MAJOR, service.system.transformToLoggedUser())
        assert netOptional.getNet() != null

        def net = netOptional.getNet()
        def roles = roleService.findAll(net.stringId)
        def roleCount = service.system.userProcessRoles.size()
        roles.each {
            service.addRole(service.system, it.stringId)
        }
        assert service.system.userProcessRoles.size() == roleCount + 3

        roles.each {
            service.removeRole(service.system, it.stringId)
        }
        assert service.system.userProcessRoles.size() == roleCount
    }
}