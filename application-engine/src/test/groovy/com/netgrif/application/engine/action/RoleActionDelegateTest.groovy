package com.netgrif.application.engine.action

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.RoleContext
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.delegate.RoleActionDelegate
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.*

class RoleActionDelegateTest {

    @Test
    void initCopiesRoleContextIntoDelegateState() {
        def delegate = delegate()
        def action = new Action()
        def user = user()
        def role = role("manager")
        def net = petriNet(role)
        def context = new RoleContext(user, role, net)

        delegate.init(action, context, [source: "unit"])

        assertSame(action, delegate.action)
        assertSame(context, delegate.actionContext)
        assertSame(role, delegate.processRole)
        assertSame(user, delegate.affectedUser)
        assertSame(net, delegate.petriNet)
        assertEquals([source: "unit"], delegate.params)
    }

    @Test
    void assignRoleByImportIdLoadsProcessAndAddsResolvedRole() {
        def delegate = delegate()
        def user = user()
        def role = role("manager")
        def net = petriNet(role)
        when(delegate.petriNetService.getDefaultVersionByIdentifier("process")).thenReturn(net)
        when(delegate.userService.addRole(user, role.stringId)).thenReturn(user)

        def result = delegate.assignRole("manager", "process", user)

        assertSame(user, result)
        verify(delegate.petriNetService).getDefaultVersionByIdentifier("process")
        verify(delegate.userService).addRole(user, role.stringId)
    }

    @Test
    void assignRoleRejectsUnknownProcess() {
        def delegate = delegate()

        def exception = assertThrows(IllegalArgumentException) {
            delegate.assignRole("manager", "missing", user())
        }

        assertEquals("The process with identifier [missing] could not be found", exception.message)
    }

    @Test
    void assignRoleUsesInitializedPetriNetAndAffectedUserByDefault() {
        def delegate = delegate()
        def user = user()
        def role = role("manager")
        delegate.affectedUser = user
        delegate.petriNet = petriNet(role)
        when(delegate.userService.addRole(user, role.stringId)).thenReturn(user)

        assertSame(user, delegate.assignRole("manager"))
        verify(delegate.userService).addRole(user, role.stringId)
    }

    @Test
    void removeRoleRemovesResolvedRoleAndSavesUser() {
        def delegate = delegate()
        def user = user()
        def role = role("manager")
        user.processRoles.add(role)
        delegate.affectedUser = user
        delegate.petriNet = petriNet(role)
        when(delegate.processRoleService.findById(role.stringId)).thenReturn(role)
        when(delegate.userService.saveUser(user)).thenReturn(user)

        def result = delegate.removeRole("manager")

        assertSame(user, result)
        assertFalse(user.processRoles.contains(role))
        verify(delegate.processRoleService).findById(role.stringId)
        verify(delegate.userService).saveUser(user)
    }

    @Test
    void removeRoleByProcessIdentifierLoadsProcessFirst() {
        def delegate = delegate()
        def user = user()
        def role = role("manager")
        user.processRoles.add(role)
        def net = petriNet(role)
        when(delegate.petriNetService.getDefaultVersionByIdentifier("process")).thenReturn(net)
        when(delegate.processRoleService.findById(role.stringId)).thenReturn(role)
        when(delegate.userService.saveUser(user)).thenReturn(user)

        assertSame(user, delegate.removeRole("manager", "process", user))
        verify(delegate.petriNetService).getDefaultVersionByIdentifier("process")
    }

    private RoleActionDelegate delegate() {
        def delegate = new RoleActionDelegate()
        delegate.userService = mock(UserService)
        delegate.petriNetService = mock(IPetriNetService)
        delegate.processRoleService = mock(ProcessRoleService)
        return delegate
    }

    private com.netgrif.application.engine.adapter.spring.auth.domain.User user() {
        def user = new com.netgrif.application.engine.adapter.spring.auth.domain.User()
        user.username = "john"
        user.email = "john@example.com"
        user.realmId = "realm"
        user.firstName = "John"
        user.lastName = "Worker"
        return user
    }

    private com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole role(String importId) {
        def role = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole(new ProcessResourceId().toString())
        role.importId = importId
        role.name = "Manager"
        return role
    }

    private com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet petriNet(
            com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole role
    ) {
        def net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet()
        net.identifier = "process"
        net.roles = [(role.stringId): role] as LinkedHashMap
        return net
    }
}
