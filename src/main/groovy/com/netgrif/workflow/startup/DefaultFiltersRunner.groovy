package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.workflow.domain.DataField
import com.netgrif.workflow.workflow.domain.QTask
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class DefaultFiltersRunner extends AbstractOrderedCommandLineRunner {

    private static final String AUTO_CREATE_TRANSITION = "auto_create"

    private static final String FILTER_TYPE_FIELD_ID = "filter_type"
    private static final String FILTER_ORIGIN_VIEW_ID_FIELD_ID = "origin_view_id"
    private static final String FILTER_VISIBILITY_FIELD_ID = "visibility"
    private static final String FILTER_FIELD_ID = "filter"

    private static final String FILTER_TYPE_CASE = "Case"
    private static final String FILTER_TYPE_TASK = "Task"

    private static final String FILTER_VISIBILITY_PUBLIC = "public"

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IUserService userService

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IDataService dataService;

    @Override
    void run(String... args) throws Exception {
        // TODO don't create default filters if they already exist
        // TODO i18n
        createCaseFilter("All cases", "assignment", "", FILTER_VISIBILITY_PUBLIC, "", [], [
                "predicateMetadata": [],
                "searchCategories": []
        ])
        createCaseFilter("My cases", "assignment_ind", "", FILTER_VISIBILITY_PUBLIC, "(author:<<me>>)", [], [
                "predicateMetadata": [[["category": "case_author", "configuration": ["operator":"equals"], "values":[["text":"search.category.userMe", value:["<<me>>"]]]]]],
                "searchCategories": ["case_author"]
        ])

        createTaskFilter("All tasks", "library_add_check", "", FILTER_VISIBILITY_PUBLIC, "", [], [
                "predicateMetadata": [],
                "searchCategories": []
        ])
        createTaskFilter("My tasks", "account_box", "", FILTER_VISIBILITY_PUBLIC, "(userId:<<me>>)", [], [
                "predicateMetadata": [[["category": "task_assignee", "configuration": ["operator":"equals"], "values":[["text":"search.category.userMe", value:["<<me>>"]]]]]],
                "searchCategories": ["task_assignee"]
        ])
    }

    public Optional<Case> createCaseFilter(String title, String icon, String filterOriginViewId, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, boolean withDefaultCategories = true) {
        return createFilter(title, icon, FILTER_TYPE_CASE, filterOriginViewId, filterVisibility, filterQuery, allowedNets, filterMetadata, withDefaultCategories)
    }

    public Optional<Case> createTaskFilter(String title, String icon, String filterOriginViewId, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, boolean withDefaultCategories = true) {
        return createFilter(title, icon, FILTER_TYPE_TASK, filterOriginViewId, filterVisibility, filterQuery, allowedNets, filterMetadata, withDefaultCategories)
    }

    private Optional<Case> createFilter(String title, String icon, String filterType, String filterOriginViewId, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, boolean withDefaultCategories) {
        return createFilter(title, icon, filterType, filterOriginViewId, filterVisibility, filterQuery, allowedNets, filterMetadata << ["filterType": filterType, "defaultSearchCategories": withDefaultCategories])
    }

    private Optional<Case> createFilter(String title, String icon, String filterType, String filterOriginViewId, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata) {
        PetriNet filterNet = this.petriNetService.getNewestVersionByIdentifier('filter')
        if (filterNet == null) {
            return Optional.empty()
        }

        Case filterCase = this.workflowService.createCase(filterNet.getStringId(), title, null, this.userService.getLoggedOrSystem().transformToLoggedUser())
        filterCase.setIcon(icon)
        filterCase = this.workflowService.save(filterCase)
        Task newFilterTask = this.taskService.searchOne(QTask.task.transitionId.eq(AUTO_CREATE_TRANSITION).and(QTask.task.caseId.eq(filterCase.getStringId())))
        this.taskService.assignTask(newFilterTask, this.userService.getLoggedOrSystem())
        this.dataService.setData(newFilterTask, ImportHelper.populateDataset([
            (FILTER_TYPE_FIELD_ID): [
                "type": "enumeration_map",
                "value": filterType
            ],
            (FILTER_VISIBILITY_FIELD_ID): [
                "type": "enumeration_map",
                "value": filterVisibility
            ],
            (FILTER_ORIGIN_VIEW_ID_FIELD_ID): [
                    "type": "text",
                    "value": filterOriginViewId
            ],
            (FILTER_FIELD_ID): [
                    "type": "filter",
                    "value": filterQuery,
                    "allowedNets": allowedNets,
                    "filterMetadata": filterMetadata
            ]
        ]))
        this.taskService.finishTask(newFilterTask, this.userService.getLoggedOrSystem())
        return Optional.of(this.workflowService.findOne(filterCase.getStringId()))
    }
}
