package com.netgrif.workflow.petrinet.domain.dataset.logic.action.delegate

import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.model.Role
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.context.RoleContext
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class RoleActionDelegate extends AbstractActionDelegate<RoleContext> {

    @Autowired
    IUserService userService

    @Autowired
    IPetriNetService petriNetService

    @Autowired
    IProcessRoleService processRoleService

    @Autowired
    IUserProcessRoleService userProcessRoleService

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

    User assignRole(ProcessRole role, User user = affectedUser) {
        String roleId = role.stringId
        return assignRole(roleId, user, petriNet)
    }

    User assignRole(String roleId, User user = affectedUser) {
        return assignRole(roleId, user, petriNet)
    }

    User assignRole(String roleImportId, String petriNetIdentifier, User user = affectedUser) {
        PetriNet petriNet = petriNetService.getNewestVersionByIdentifier(petriNetIdentifier)
        assignRole(roleImportId, user, petriNet)
    }

    User assignRole(String roleImportId, User user = affectedUser, PetriNet petriNet) {
        Map<String, ProcessRole> map = petriNet.getRoles()
        def foundEntry = map.find {entry ->
            entry.value.importId == roleImportId
        }

        String roleId = foundEntry.key
        userService.addRole(user, roleId)
    }

    User removeRole(ProcessRole role, User user = affectedUser) {
        String roleId = role.stringId
        return removeRole(roleId, user)
    }

    User removeRole(String roleId, User user = affectedUser) {
        return removeRole(roleId, user, petriNet)
    }

    User removeRole(String roleImportId, String petriNetIdentifier, User user = affectedUser) {
        PetriNet petriNet = petriNetService.getNewestVersionByIdentifier(petriNetIdentifier)
        removeRole(roleImportId, user, petriNet)
    }

    User removeRole(String roleImportId, User user = affectedUser, PetriNet petriNet) {
        Map<String, ProcessRole> map = petriNet.getRoles()
        def foundEntry = map.find {entry ->
            entry.value.importId == roleImportId
        }

        String roleId = foundEntry.key
        UserProcessRole role = userProcessRoleService.findByRoleId(roleId)

        user.getUserProcessRoles().remove(role)
        return userService.save(user)
    }
}
