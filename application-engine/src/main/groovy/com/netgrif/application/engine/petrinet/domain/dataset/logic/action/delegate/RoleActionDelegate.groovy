package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.delegate


import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.RoleContext
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class RoleActionDelegate extends AbstractActionDelegate<RoleContext> {

    @Autowired
    UserService userService

    @Autowired
    IPetriNetService petriNetService

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

    AbstractUser assignRole(ProcessRole role, AbstractUser user = affectedUser) {
        String roleId = role.stringId
        return assignRole(roleId, user, petriNet)
    }

    AbstractUser assignRole(String roleId, AbstractUser user = affectedUser) {
        return assignRole(roleId, user, petriNet)
    }

    AbstractUser assignRole(String roleImportId, String petriNetIdentifier, AbstractUser user = affectedUser) {
        PetriNet petriNet = petriNetService.getDefaultVersionByIdentifier(petriNetIdentifier)
        if (petriNet == null) {
            throw new IllegalArgumentException("The process with identifier [%s] could not be found".formatted(petriNetIdentifier))
        }
        assignRole(roleImportId, user, petriNet)
    }

    AbstractUser assignRole(String roleImportId, AbstractUser user = affectedUser, PetriNet petriNet) {
        Map<String, ProcessRole> map = petriNet.getRoles()
        def foundEntry = map.find { entry ->
            entry.value.importId == roleImportId
        }

        String roleId = foundEntry.key
        userService.addRole(user, roleId)
    }

    AbstractUser removeRole(ProcessRole role, AbstractUser user = affectedUser) {
        String roleId = role.stringId
        return removeRole(roleId, user)
    }

    AbstractUser removeRole(String roleId, AbstractUser user = affectedUser) {
        return removeRole(roleId, user, petriNet)
    }

    AbstractUser removeRole(String roleImportId, String petriNetIdentifier, AbstractUser user = affectedUser) {
        PetriNet petriNet = petriNetService.getDefaultVersionByIdentifier(petriNetIdentifier)
        if (petriNet == null) {
            throw new IllegalArgumentException("The process with identifier [%s] could not be found".formatted(petriNetIdentifier))
        }
        removeRole(roleImportId, user, petriNet)
    }

    AbstractUser removeRole(String roleImportId, AbstractUser user = affectedUser, PetriNet petriNet) {
        Map<String, ProcessRole> map = petriNet.getRoles()
        def foundEntry = map.find { entry ->
            entry.value.importId == roleImportId
        }

        String roleId = foundEntry.key
        ProcessRole role = processRoleService.findById(roleId)

        user.getProcessRoles().remove(role)
        return userService.saveUser(user)
    }
}
