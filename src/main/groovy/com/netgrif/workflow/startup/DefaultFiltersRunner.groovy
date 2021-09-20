package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.workflow.domain.QCase
import com.netgrif.workflow.workflow.domain.QTask
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Slf4j
@Component
class DefaultFiltersRunner extends AbstractOrderedCommandLineRunner {

    private static final String AUTO_CREATE_TRANSITION = "auto_create"

    private static final String FILTER_TYPE_FIELD_ID = "filter_type"
    private static final String FILTER_ORIGIN_VIEW_ID_FIELD_ID = "origin_view_id"
    private static final String FILTER_VISIBILITY_FIELD_ID = "visibility"
    private static final String FILTER_FIELD_ID = "filter"
    private static final String FILTER_I18N_TITLE_FIELD_ID = "i18n_filter_name"
    private static final String GERMAN_ISO_3166_CODE = "de"
    private static final String SLOVAK_ISO_3166_CODE = "sk"

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
    private ITaskService taskService

    @Autowired
    private IDataService dataService

    @Override
    void run(String... args) throws Exception {
        createCaseFilter("All cases", "assignment", "", FILTER_VISIBILITY_PUBLIC, "", [], [
                "predicateMetadata": [],
                "searchCategories": []
        ], [
                (GERMAN_ISO_3166_CODE): "Alle Fälle",
                (SLOVAK_ISO_3166_CODE): "Všetky prípady"
        ])
        createCaseFilter("My cases", "assignment_ind", "", FILTER_VISIBILITY_PUBLIC, "(author:<<me>>)", [], [
                "predicateMetadata": [[["category": "case_author", "configuration": ["operator":"equals"], "values":[["text":"search.category.userMe", value:["<<me>>"]]]]]],
                "searchCategories": ["case_author"]
        ], [
                (GERMAN_ISO_3166_CODE): "Meine Fälle",
                (SLOVAK_ISO_3166_CODE): "Moje prípady"
        ])

        createTaskFilter("All tasks", "library_add_check", "", FILTER_VISIBILITY_PUBLIC, "", [], [
                "predicateMetadata": [],
                "searchCategories": []
        ], [
                (GERMAN_ISO_3166_CODE): "Alle Aufgaben",
                (SLOVAK_ISO_3166_CODE): "Všetky úlohy"
        ])
        createTaskFilter("My tasks", "account_box", "", FILTER_VISIBILITY_PUBLIC, "(userId:<<me>>)", ["prva_siet"], [
                "predicateMetadata": [[["category": "task_assignee", "configuration": ["operator":"equals"], "values":[["text":"search.category.userMe", value:["<<me>>"]]]]]],
                "searchCategories": ["task_assignee"]
        ], [
                (GERMAN_ISO_3166_CODE): "Meine Aufgaben",
                (SLOVAK_ISO_3166_CODE): "Moje úlohy"
        ])
        createCaseFilter("Test filter", "filter_alt", "", FILTER_VISIBILITY_PUBLIC,
                "((((dataSet.number.numberValue:5) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.number.numberValue:[10 TO 100]) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.text.fulltextValue:*asdad*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) " +
                        "OR ((dataSet.enumeration.fulltextValue:*asdasd*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.enumeration_map.fulltextValue:*asdasd*) " +
                        "AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.multichoice.fulltextValue:*asdasd*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) " +
                        "OR ((dataSet.boolean.booleanValue:true) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.boolean.booleanValue:false) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.date.timestampValue:[1631138400000 TO 1631224800000}) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.date.timestampValue:[1631138400000 TO 1631311200000}) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.file.fileNameValue:*asdasd*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR " +
                        "((dataSet.fileList.fileNameValue:*asdasd*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.user.userIdValue:<<me>>) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.user.userIdValue:7) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) " +
                        "OR ((dataSet.datetime.timestampValue:[1631184300000 TO 1631184360000}) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR " +
                        "((dataSet.datetime.timestampValue:[1631184360000 TO 1631270820000}) AND (processIdentifier:6139e51308215f25b0a498c2_all_data))) AND (title:*asdasd*) AND " +
                        "((creationDateSortable:[1631138400000 TO 1631224800000}) OR (creationDateSortable:[1631138400000 TO 1631311200000})) AND " +
                        "((creationDateSortable:[1631184360000 TO 1631184420000}) OR (creationDateSortable:[1631184360000 TO 1631270820000})) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data) AND ((taskIds:1) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) AND " +
                        "((author:<<me>>) OR (!(author:7))) AND (visualId:*asdad*) AND (stringId:*asdasd*))", ["all_data"],
                ["predicateMetadata": [
                        [[
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "equals",
                                "datafield": "number#Number"
                            ],
                             "values": [5]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "in_range",
                                "datafield": "number#Number"
                            ],
                             "values": [10, 100]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "substring",
                                "datafield": "text#Text"
                            ],
                             "values": ["asdad"]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "substring",
                                "datafield": "enumeration#Enumeration"
                            ],
                             "values": ["asdasd"]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "substring",
                                "datafield": "enumeration_map#Enumeration Map"
                            ],
                             "values": ["asdasd"]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "substring",
                                "datafield": "multichoice#Multichoice"
                            ],
                             "values": ["asdasd"]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "equals",
                                "datafield": "boolean#Boolean"
                            ],
                             "values": [true]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "equals",
                                "datafield": "boolean#Boolean"
                            ],
                             "values": [false]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "equals_date",
                                "datafield": "date#Date"
                            ],
                             "values": [1631138400000]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "in_range_date",
                                "datafield": "date#Date"
                            ],
                             "values": [1631138400000, 1631224800000]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "substring",
                                "datafield": "file#File"
                            ],
                             "values": ["asdasd"]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "substring",
                                "datafield": "fileList#File List"
                            ],
                             "values": ["asdasd"]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "equals",
                                "datafield": "user#User"
                            ],
                             "values": [[
                                            "text": "search.category.userMe",
                                            "value": ["<<me>>"]
                                        ]]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "equals",
                                "datafield": "user#User"
                            ],
                             "values": [[
                                            "text": "Admin Netgrif",
                                            "value": [7]
                                        ]]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "equals_date_time",
                                "datafield": "dateTime#Datetime"
                            ],
                             "values": [1631184356623]
                         ], [
                             "category": "case_dataset",
                             "configuration": [
                                "operator": "in_range_date_time",
                                "datafield": "dateTime#Datetime"
                            ],
                             "values": [1631184364266, 1631270767000]
                         ]],
                        [[
                             "category": "case_title",
                             "configuration": [
                                "operator": "substring"
                            ],
                             "values": ["asdasd"]
                         ]],
                        [[
                             "category": "case_creation_date",
                             "configuration": [
                                "operator": "equals_date"
                            ],
                             "values": [1631138400000]
                         ], [
                             "category": "case_creation_date",
                             "configuration": [
                                "operator": "in_range_date"
                            ],
                             "values": [1631138400000, 1631224800000]
                         ]],
                        [[
                             "category": "case_creation_date_time",
                             "configuration": [
                                "operator": "equals_date_time"
                            ],
                             "values": [1631184402526]
                         ], [
                             "category": "case_creation_date_time",
                             "configuration": [
                                "operator": "in_range_date_time"
                            ],
                             "values": [1631184408995, 1631270810000]
                         ]],
                        [[
                             "category": "case_process",
                             "configuration": [
                                "operator": "equals"
                            ],
                             "values": ["All Data"]
                         ]],
                        [[
                             "category": "case_task",
                             "configuration": [
                                "operator": "equals"
                            ],
                             "values": ["Task - editable"]
                         ]],
                        [[
                             "category": "case_author",
                             "configuration": [
                                "operator": "equals"
                            ],
                             "values": [[
                                            "text": "search.category.userMe",
                                            "value": ["<<me>>"]
                                        ]]
                         ], [
                             "category": "case_author",
                             "configuration": [
                                "operator": "not_equals"
                            ],
                             "values": [[
                                            "text": "Admin Netgrif",
                                            "value": [7]
                                        ]]
                         ]],
                        [[
                             "category": "case_visual_id",
                             "configuration": [
                                "operator": "substring"
                            ],
                             "values": ["asdad"]
                         ]],
                        [[
                             "category": "case_string_id",
                             "configuration": [
                                "operator": "substring"
                            ],
                             "values": ["asdasd"]
                         ]]
                ],
                "searchCategories": ["case_dataset", "case_title", "case_creation_date", "case_creation_date_time", "case_process", "case_task", "case_author", "case_visual_id", "case_string_id"]],
                [
                        (GERMAN_ISO_3166_CODE): "Dig",
                        (SLOVAK_ISO_3166_CODE): "Dilino"
                ]
        )
    }

    /**
     * Creates a new case filter filter process instance
     * @param title unique title of the default filter
     * @param icon material icon identifier of the default filter
     * @param filterOriginViewId viewID of the view the filter originated in
     * @param filterVisibility filter visibility
     * @param filterQuery the elastic query string query used by the filter
     * @param allowedNets list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata metadata of the serialised filter as generated by the frontend
     * @param titleTranslations a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createCaseFilter(
            String title,
            String icon,
            String filterOriginViewId,
            String filterVisibility,
            String filterQuery,
            List<String> allowedNets,
            Map<String, Object> filterMetadata,
            Map<String, String> titleTranslations,
            boolean withDefaultCategories = true,
            boolean inheritBaseAllowedNets = true
    ) {
        return createFilter(title, icon, FILTER_TYPE_CASE, filterOriginViewId, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets)
    }

    /**
     * Creates a new task filter filter process instance
     * @param title unique title of the default filter
     * @param icon material icon identifier of the default filter
     * @param filterOriginViewId viewID of the view the filter originated in
     * @param filterVisibility filter visibility
     * @param filterQuery the elastic query string query used by the filter
     * @param allowedNets list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata metadata of the serialised filter as generated by the frontend
     * @param titleTranslations a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createTaskFilter(
            String title,
            String icon,
            String filterOriginViewId,
            String filterVisibility,
            String filterQuery,
            List<String> allowedNets,
            Map<String, Object> filterMetadata,
            Map<String, String> titleTranslations,
            boolean withDefaultCategories = true,
            boolean inheritBaseAllowedNets = true
    ) {
        return createFilter(title, icon, FILTER_TYPE_TASK, filterOriginViewId, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets)
    }

    private Optional<Case> createFilter(
            String title,
            String icon,
            String filterType,
            String filterOriginViewId,
            String filterVisibility,
            String filterQuery,
            List<String> allowedNets,
            Map<String, Object> filterMetadata,
            Map<String, String> titleTranslations,
            boolean withDefaultCategories,
            boolean inheritBaseAllowedNets
    ) {
        return createFilter(
                title,
                icon,
                filterType,
                filterOriginViewId,
                filterVisibility,
                filterQuery,
                allowedNets,
                filterMetadata << ["filterType": filterType, "defaultSearchCategories": withDefaultCategories, "inheritAllowedNets": inheritBaseAllowedNets],
                titleTranslations
        )
    }

    private Optional<Case> createFilter(
            String title,
            String icon,
            String filterType,
            String filterOriginViewId,
            String filterVisibility,
            String filterQuery,
            List<String> allowedNets,
            Map<String, Object> filterMetadata,
            Map<String, String> titleTranslations
    ) {
        PetriNet filterNet = this.petriNetService.getNewestVersionByIdentifier('filter')
        if (filterNet == null) {
            return Optional.empty()
        }

        def systemUser = this.userService.getLoggedOrSystem()

        def existingFilter = this.workflowService.search(QCase.case$.processIdentifier.eq("filter") & QCase.case$.author.id.eq(systemUser.getId()) & QCase.case$.title.eq(title), PageRequest.of(0, 1))
        if (existingFilter.totalElements == 1) {
            return Optional.of(existingFilter.getContent()[0])
        }

        Case filterCase = this.workflowService.createCase(filterNet.getStringId(), title, null, systemUser.transformToLoggedUser())
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

        I18nString translatedTitle = new I18nString(title)
        titleTranslations.forEach({locale, translation -> translatedTitle.addTranslation(locale, translation)})

        filterCase = this.workflowService.findOne(filterCase.getStringId())
        filterCase.dataSet[FILTER_I18N_TITLE_FIELD_ID].value = translatedTitle
        workflowService.save(filterCase)

        this.taskService.finishTask(newFilterTask, this.userService.getLoggedOrSystem())
        return Optional.of(this.workflowService.findOne(filterCase.getStringId()))
    }
}
