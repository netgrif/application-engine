package com.netgrif.workflow.petrinet.domain.dataset.logic.action.context

import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole

class RoleContext<T> extends ActionContext {

    T user
    ProcessRole role
    PetriNet petriNet

    RoleContext(T user, ProcessRole role, PetriNet petriNet) {
        this.user = user
        this.role = role
        this.petriNet = petriNet
    }
}
