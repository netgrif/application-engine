package com.netgrif.workflow.petrinet.domain.dataset.logic.action.delegate

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.context.ActionContext

abstract class AbstractActionDelegate<T extends ActionContext> {

    Action action
    T actionContext
}
