package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.delegate

import com.netgrif.core.auth.domain.IUser

import com.netgrif.adapter.auth.service.UserService
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.core.petrinet.domain.dataset.logic.action.Action
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.RoleContext
import com.netgrif.adapter.petrinet.service.PetriNetService
import com.netgrif.adapter.petrinet.service.ProcessRoleService
import com.netgrif.core.petrinet.domain.roles.ProcessRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class RoleActionDelegate extends AbstractActionDelegate<RoleContext> {

    @Autowired
    UserService userService

    @Autowired
    PetriNetService petriNetService

    @Autowired
    ProcessRoleService processRoleService

    Action action
    ProcessRole processRole
    Map<String, String> params
    PetriNet petriNet
    def affectedUser

    def init(Action action, RoleContext roleContext, Map<String, String> params = [:]) {
        this.action = action
        this.actionContext = actionContext
        this.params = params

        this.processRole = roleContext.role
        this.affectedUser = roleContext.user
        this.petriNet = roleContext.petriNet
    }

    IUser assignRole(ProcessRole role, IUser user = affectedUser) {
        String roleId = role.stringId
        return assignRole(roleId, user, petriNet)
    }

    IUser assignRole(String roleId, IUser user = affectedUser) {
        return assignRole(roleId, user, petriNet)
    }

    IUser assignRole(String roleImportId, String petriNetIdentifier, IUser user = affectedUser) {
        PetriNet petriNet = petriNetService.getNewestVersionByIdentifier(petriNetIdentifier)
        assignRole(roleImportId, user, petriNet)
    }

    IUser assignRole(String roleImportId, IUser user = affectedUser, PetriNet petriNet) {
        Map<String, ProcessRole> map = petriNet.getRoles()
        def foundEntry = map.find { entry ->
            entry.value.importId == roleImportId
        }

        String roleId = foundEntry.key
        userService.addRole(user, roleId)
    }

    IUser removeRole(ProcessRole role, IUser user = affectedUser) {
        String roleId = role.stringId
        return removeRole(roleId, user)
    }

    IUser removeRole(String roleId, IUser user = affectedUser) {
        return removeRole(roleId, user, petriNet)
    }

    IUser removeRole(String roleImportId, String petriNetIdentifier, IUser user = affectedUser) {
        PetriNet petriNet = petriNetService.getNewestVersionByIdentifier(petriNetIdentifier)
        removeRole(roleImportId, user, petriNet)
    }

    IUser removeRole(String roleImportId, IUser user = affectedUser, PetriNet petriNet) {
        Map<String, ProcessRole> map = petriNet.getRoles()
        def foundEntry = map.find { entry ->
            entry.value.importId == roleImportId
        }

        String roleId = foundEntry.key
        ProcessRole role = processRoleService.findById(roleId)

        user.getProcessRoles().remove(role)
        return userService.saveUser(user, null)
    }
}
