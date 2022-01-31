package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.delegate

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.ActionContext

abstract class AbstractActionDelegate<T extends ActionContext> {

    Action action
    T actionContext
}
