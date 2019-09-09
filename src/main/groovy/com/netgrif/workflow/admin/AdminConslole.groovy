package com.netgrif.workflow.admin

import com.netgrif.workflow.AsyncRunner
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.service.FieldFactory
import com.netgrif.workflow.orgstructure.service.GroupService
import com.netgrif.workflow.orgstructure.service.MemberService
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.TaskService
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.springframework.beans.factory.annotation.Autowired

@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class AdminConslole {

    @Autowired
    FieldFactory fieldFactory

    @Autowired
    TaskService taskService

    @Autowired
    IDataService dataService

    @Autowired
    IWorkflowService workflowService

    @Autowired
    IUserService userService

    @Autowired
    IPetriNetService petriNetService

    @Autowired
    AsyncRunner async

    @Autowired
    GroupService groupService

    @Autowired
    MemberService memberService

    def init(Action action, Case, FieldActionsRunner actionsRunner) {
        this.action = action
        this.actionsRunner = actionsRunner
        action.fieldIds.each { name, id ->
            set(name, fieldFactory.buildFieldWithoutValidation(useCase, id))
        }
        action.transitionIds.each { name, id ->
            set(name, useCase.petriNet.transitions[id])
        }
    }

}
