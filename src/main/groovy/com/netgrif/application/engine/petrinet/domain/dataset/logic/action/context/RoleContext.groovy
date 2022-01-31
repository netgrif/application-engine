package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context

import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole

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
