package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.FilterField
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value

@Slf4j
@Component
class DefaultFiltersRunner extends AbstractOrderedCommandLineRunner {

    public static final String AUTO_CREATE_TRANSITION = "auto_create"
    public static final String DETAILS_TRANSITION = "t2"

    public static final String FILTER_TYPE_FIELD_ID = "filter_type"
    public static final String FILTER_ORIGIN_VIEW_ID_FIELD_ID = "origin_view_id"
    public static final String FILTER_PARENT_CASE_ID_FIELD_ID = "parent_filter_id"
    public static final String FILTER_VISIBILITY_FIELD_ID = "visibility"
    public static final String FILTER_FIELD_ID = "filter"
    public static final String FILTER_I18N_TITLE_FIELD_ID = "i18n_filter_name"
    public static final String GERMAN_ISO_3166_CODE = "de"
    public static final String SLOVAK_ISO_3166_CODE = "sk"
    public static final String IS_IMPORTED = "is_imported"

    public static final String FILTER_TYPE_CASE = "Case"
    public static final String FILTER_TYPE_TASK = "Task"

    public static final String FILTER_VISIBILITY_PRIVATE = "private"
    public static final String FILTER_VISIBILITY_PUBLIC = "public"

    @Value('${nae.create.default.filters:false}')
    private Boolean createDefaultFilters

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IUserService userService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IDataService dataService

    @Autowired
    private SuperCreator superCreator

    @Override
    void run(String... args) throws Exception {
        if (!createDefaultFilters) {
            return
        }
        createCaseFilter("All cases", "assignment", FILTER_VISIBILITY_PUBLIC, "", [], [
                "predicateMetadata": [],
                "searchCategories" : []
        ], [
                (GERMAN_ISO_3166_CODE): "Alle Fälle",
                (SLOVAK_ISO_3166_CODE): "Všetky prípady"
        ])
        createCaseFilter("My cases", "assignment_ind", FILTER_VISIBILITY_PUBLIC, "(author:<<me>>)", [], [
                "predicateMetadata": [[["category": "case_author", "configuration": ["operator": "equals"], "values": [["text": "search.category.userMe", value: ["<<me>>"]]]]]],
                "searchCategories" : ["case_author"]
        ], [
                (GERMAN_ISO_3166_CODE): "Meine Fälle",
                (SLOVAK_ISO_3166_CODE): "Moje prípady"
        ])

        createTaskFilter("All tasks", "library_add_check", FILTER_VISIBILITY_PUBLIC, "", [], [
                "predicateMetadata": [],
                "searchCategories" : []
        ], [
                (GERMAN_ISO_3166_CODE): "Alle Aufgaben",
                (SLOVAK_ISO_3166_CODE): "Všetky úlohy"
        ])
        createTaskFilter("My tasks", "account_box", FILTER_VISIBILITY_PUBLIC, "(userId:<<me>>)", [], [
                "predicateMetadata": [[["category": "task_assignee", "configuration": ["operator": "equals"], "values": [["text": "search.category.userMe", value: ["<<me>>"]]]]]],
                "searchCategories" : ["task_assignee"]
        ], [
                (GERMAN_ISO_3166_CODE): "Meine Aufgaben",
                (SLOVAK_ISO_3166_CODE): "Moje úlohy"
        ])
    }

    /**
     * Creates a new case filter filter process instance
     * @param title unique title of the default filter
     * @param icon material icon identifier of the default filter
     * @param filterVisibility filter visibility
     * @param filterQuery the elastic query string query used by the filter
     * @param allowedNets list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata metadata of the serialised filter as generated by the frontend
     * @param titleTranslations a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId the ID of the parent if any
     * @param viewOrigin true if the parent was a view. false if the parent was another filter
     * @param isImported whether the filter is being created by importing it from na XML file
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    Optional<Case> createCaseFilter(
            String title,
            String icon,
            String filterVisibility,
            String filterQuery,
            List<String> allowedNets,
            Map<String, Object> filterMetadata,
            Map<String, String> titleTranslations,
            boolean withDefaultCategories = true,
            boolean inheritBaseAllowedNets = true,
            String originId = null,
            boolean viewOrigin = false,
            boolean isImported = false
    ) {
        return createFilter(
                title,
                icon,
                FILTER_TYPE_CASE,
                filterVisibility,
                filterQuery,
                allowedNets,
                filterMetadata,
                titleTranslations,
                withDefaultCategories,
                inheritBaseAllowedNets,
                originId,
                viewOrigin,
                isImported
        )
    }

    /**
     * Creates a new task filter filter process instance
     * @param title unique title of the default filter
     * @param icon material icon identifier of the default filter
     * @param filterVisibility filter visibility
     * @param filterQuery the elastic query string query used by the filter
     * @param allowedNets list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata metadata of the serialised filter as generated by the frontend
     * @param titleTranslations a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId the ID of the parent if any
     * @param viewOrigin true if the parent was a view. false if the parent was another filter
     * @param isImported whether the filter is being created by importing it from na XML file
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    Optional<Case> createTaskFilter(
            String title,
            String icon,
            String filterVisibility,
            String filterQuery,
            List<String> allowedNets,
            Map<String, Object> filterMetadata,
            Map<String, String> titleTranslations,
            boolean withDefaultCategories = true,
            boolean inheritBaseAllowedNets = true,
            String originId = null,
            boolean viewOrigin = false,
            boolean isImported = false
    ) {
        return createFilter(
                title,
                icon,
                FILTER_TYPE_TASK,
                filterVisibility,
                filterQuery,
                allowedNets,
                filterMetadata,
                titleTranslations,
                withDefaultCategories,
                inheritBaseAllowedNets,
                originId,
                viewOrigin,
                isImported
        )
    }

    /**
     * Creates a new filter process instance of the provided type
     * @param title unique title of the default filter
     * @param icon material icon identifier of the default filter
     * @param filterType the type of the filter
     * @param filterVisibility filter visibility
     * @param filterQuery the elastic query string query used by the filter
     * @param allowedNets list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata metadata of the serialised filter as generated by the frontend
     * @param titleTranslations a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId the ID of the parent if any
     * @param viewOrigin true if the parent was a view. false if the parent was another filter
     * @param isImported whether the filter is being created by importing it from na XML file
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    Optional<Case> createFilter(
            String title,
            String icon,
            String filterType,
            String filterVisibility,
            String filterQuery,
            List<String> allowedNets,
            Map<String, Object> filterMetadata,
            Map<String, String> titleTranslations,
            boolean withDefaultCategories,
            boolean inheritBaseAllowedNets,
            String originId = null,
            boolean viewOrigin = false,
            boolean isImported = false
    ) {
        return createFilterCase(
                title,
                icon,
                filterType,
                filterVisibility,
                filterQuery,
                allowedNets,
                filterMetadata << ["filterType": filterType, "defaultSearchCategories": withDefaultCategories, "inheritAllowedNets": inheritBaseAllowedNets],
                titleTranslations,
                originId,
                viewOrigin,
                isImported
        )
    }

    private Optional<Case> createFilterCase(
            String title,
            String icon,
            String filterType,
            String filterVisibility,
            String filterQuery,
            List<String> allowedNets,
            Map<String, Object> filterMetadata,
            Map<String, String> titleTranslations,
            String originId,
            boolean viewOrigin,
            boolean isImported
    ) {
        PetriNet filterNet = this.petriNetService.getNewestVersionByIdentifier('filter')
        if (filterNet == null) {
            return Optional.empty()
        }

        def loggedUser = this.userService.getLoggedOrSystem()

        if (loggedUser.getStringId() == this.userService.getSystem().getStringId()) {
            Case filterCase = this.workflowService.searchOne(QCase.case$.processIdentifier.eq("filter").and(QCase.case$.title.eq(title)).and(QCase.case$.author.id.eq(userService.getSystem().getStringId())))
            if (filterCase != null) {
                return Optional.of(filterCase)
            }
        }

        Case filterCase = this.workflowService.createCase(filterNet.getStringId(), title, null, loggedUser.transformToLoggedUser()).getCase()
        filterCase.setIcon(icon)
        filterCase = this.workflowService.save(filterCase)
        Task newFilterTask = this.taskService.searchOne(QTask.task.transitionId.eq(AUTO_CREATE_TRANSITION).and(QTask.task.caseId.eq(filterCase.getStringId())))
        this.taskService.assignTask(newFilterTask, this.userService.getLoggedOrSystem())

        DataSet dataSet = new DataSet([
                (FILTER_TYPE_FIELD_ID): new EnumerationMapField(rawValue: filterType),
                (FILTER_VISIBILITY_FIELD_ID): new EnumerationMapField(rawValue: filterVisibility),
                (FILTER_FIELD_ID): new FilterField(rawValue: filterQuery, allowedNets: allowedNets, filterMetadata: filterMetadata)
        ] as Map<String, Field<?>>)

        if (originId != null) {
            dataSet.put(viewOrigin ? FILTER_ORIGIN_VIEW_ID_FIELD_ID : FILTER_PARENT_CASE_ID_FIELD_ID, new TextField(rawValue: originId))
        }

        // TODO: release/8.0.0 join setData to one call
        this.dataService.setData(newFilterTask, dataSet, superCreator.getSuperUser())
        if (isImported) {
            this.dataService.setData(newFilterTask, new DataSet([
                    (IS_IMPORTED): new NumberField(rawValue: 1)
            ] as Map<String, Field<?>>), superCreator.getSuperUser())
        }

        I18nString translatedTitle = new I18nString(title)
        titleTranslations.forEach({locale, translation -> translatedTitle.addTranslation(locale, translation)})

        filterCase = this.workflowService.findOne(filterCase.getStringId())
        filterCase.dataSet.get(FILTER_I18N_TITLE_FIELD_ID).rawValue = translatedTitle
        workflowService.save(filterCase)

        this.taskService.finishTask(newFilterTask, this.userService.getLoggedOrSystem())
        return Optional.of(this.workflowService.findOne(filterCase.getStringId()))
    }
}
