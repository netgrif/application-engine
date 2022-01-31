package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.delegate

import com.netgrif.application.engine.auth.domain.IUser

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.RoleContext
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class RoleActionDelegate extends AbstractActionDelegate<RoleContext> {

    @Autowired
    IUserService userService

    @Autowired
    IPetriNetService petriNetService

    @Autowired
    IProcessRoleService processRoleService

    Action action
    ProcessRole processRole
    PetriNet petriNet
    def affectedUser

    def init(Action action, RoleContext roleContext) {
        this.action = action
        this.actionContext = actionContext

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
        return userService.save(user)
    }
}
