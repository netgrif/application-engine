package com.netgrif.workflow.auth

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.workflow.startup.ImportHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import sun.awt.X11.XSystemTrayPeer

@RunWith(SpringRunner.class)
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
    void removeRole() {
        helper.truncateDbs()

        def netOptional = importHelper.createNet("role_test.xml", VersionType.MAJOR, service.system.transformToLoggedUser())
        assert netOptional.isPresent()

        def net = netOptional.get()
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