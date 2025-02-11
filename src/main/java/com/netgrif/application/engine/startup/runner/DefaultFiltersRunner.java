package com.netgrif.application.engine.startup.runner;

import com.netgrif.core.auth.domain.IUser;
import com.netgrif.adapter.auth.service.UserService;
import com.netgrif.core.petrinet.domain.I18nString;
import com.netgrif.core.petrinet.domain.PetriNet;
import com.netgrif.adapter.petrinet.service.PetriNetService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.adapter.workflow.domain.QCase;
import com.netgrif.core.workflow.domain.QTask;
import com.netgrif.core.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.core.auth.domain.LoggedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RunnerOrder(120)
@RequiredArgsConstructor
public class DefaultFiltersRunner implements ApplicationEngineStartupRunner {

    public static final String AUTO_CREATE_TRANSITION = "auto_create";
    public static final String DETAILS_TRANSITION = "t2";
    public static final String FILTER_TYPE_FIELD_ID = "filter_type";
    public static final String FILTER_ORIGIN_VIEW_ID_FIELD_ID = "origin_view_id";
    public static final String FILTER_PARENT_CASE_ID_FIELD_ID = "parent_filter_id";
    public static final String FILTER_VISIBILITY_FIELD_ID = "visibility";
    public static final String FILTER_FIELD_ID = "filter";
    public static final String FILTER_I18N_TITLE_FIELD_ID = "i18n_filter_name";
    public static final String GERMAN_ISO_3166_CODE = "de";
    public static final String SLOVAK_ISO_3166_CODE = "sk";
    public static final String IS_IMPORTED = "is_imported";
    public static final String FILTER_TYPE_CASE = "Case";
    public static final String FILTER_TYPE_TASK = "Task";
    public static final String FILTER_VISIBILITY_PRIVATE = "private";
    public static final String FILTER_VISIBILITY_PUBLIC = "public";

    @Value("${nae.create.default.filters:false}")
    private Boolean createDefaultFilters;

    private final PetriNetService petriNetService;
    private final IWorkflowService workflowService;
    private final UserService userService;
    private final ITaskService taskService;
    private final IDataService dataService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!createDefaultFilters) return;
        // All cases
        createCaseFilter("All cases", "assignment", FILTER_VISIBILITY_PUBLIC, "", new ArrayList<String>(),
                Map.of(
                        "predicateMetadata", new ArrayList<>(),
                        "searchCategories", new ArrayList<>()),
                Map.of(
                        GERMAN_ISO_3166_CODE, "Alle Fälle",
                        SLOVAK_ISO_3166_CODE, "Všetky prípady"));

        // My cases
        createCaseFilter("My cases", "assignment_ind", FILTER_VISIBILITY_PUBLIC, "(author:<<me>>)", new ArrayList<String>(),
                Map.of(
                        "predicateMetadata", List.of(List.of(Map.of(
                                "category", "case_author",
                                "configuration", Map.of("operator", "equals"),
                                "values", List.of(Map.of(
                                        "text", "search.category.userMe",
                                        "value", List.of("<<me>>")
                                ))
                        ))),
                        "searchCategories", List.of("case_author")
                ), Map.of(
                        GERMAN_ISO_3166_CODE, "Meine Fälle",
                        SLOVAK_ISO_3166_CODE, "Moje prípady"
                ));

        // All tasks
        createTaskFilter("All tasks", "library_add_check", FILTER_VISIBILITY_PUBLIC, "", new ArrayList<String>(),
                Map.of(
                        "predicateMetadata", List.of(),
                        "searchCategories", List.of()
                ),
                Map.of(
                        GERMAN_ISO_3166_CODE, "Alle Aufgaben",
                        SLOVAK_ISO_3166_CODE, "Všetky úlohy"
                ));

        // My tasks
        createTaskFilter("My tasks", "account_box", FILTER_VISIBILITY_PUBLIC, "(userId:<<me>>)", new ArrayList<String>(),
                Map.of(
                        "predicateMetadata", List.of(List.of(Map.of(
                                "category", "task_assignee",
                                "configuration", Map.of("operator", "equals"),
                                "values", List.of(Map.of(
                                        "text", "search.category.userMe",
                                        "value", List.of("<<me>>")
                                ))
                        ))),
                        "searchCategories", List.of("task_assignee")
                ), Map.of(
                        GERMAN_ISO_3166_CODE, "Meine Aufgaben",
                        SLOVAK_ISO_3166_CODE, "Moje úlohy"
                ));
    }

    /**
     * Creates a new case filter process instance
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @param viewOrigin             true if the parent was a view. false if the parent was another filter
     * @param isImported             whether the filter is being created by importing it from na XML file
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createCaseFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId, boolean viewOrigin, boolean isImported) {
        return createFilter(title, icon, FILTER_TYPE_CASE, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, originId, viewOrigin, isImported);
    }

    /**
     * Creates a new case filter process instance
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @param viewOrigin             true if the parent was a view. false if the parent was another filter
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createCaseFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId, boolean viewOrigin) {
        return createCaseFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, originId, viewOrigin, false);
    }

    /**
     * Creates a new case filter process instance
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createCaseFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId) {
        return createCaseFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, originId, false, false);
    }

    /**
     * Creates a new case filter process instance
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createCaseFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets) {
        return createCaseFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, null, false, false);
    }

    /**
     * Creates a new case filter process instance
     *
     * @param title                 unique title of the default filter
     * @param icon                  material icon identifier of the default filter
     * @param filterVisibility      filter visibility
     * @param filterQuery           the elastic query string query used by the filter
     * @param allowedNets           list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata        metadata of the serialised filter as generated by the frontend
     * @param titleTranslations     a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories whether the default search categories should be merged with the search categories specified in the metadata
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createCaseFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories) {
        return createCaseFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, true, null, false, false);
    }

    /**
     * Creates a new case filter process instance
     *
     * @param title             unique title of the default filter
     * @param icon              material icon identifier of the default filter
     * @param filterVisibility  filter visibility
     * @param filterQuery       the elastic query string query used by the filter
     * @param allowedNets       list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata    metadata of the serialised filter as generated by the frontend
     * @param titleTranslations a map of locale codes to translated strings for the filter title
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createCaseFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations) {
        return createCaseFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, true, true, null, false, false);
    }

    /**
     * Creates a new task filter filter process instance
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @param viewOrigin             true if the parent was a view. false if the parent was another filter
     * @param isImported             whether the filter is being created by importing it from na XML file
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createTaskFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId, boolean viewOrigin, boolean isImported) {
        return createFilter(title, icon, FILTER_TYPE_TASK, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, originId, viewOrigin, isImported);
    }

    /**
     * Creates a new task filter process instance
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @param viewOrigin             true if the parent was a view. false if the parent was another filter
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createTaskFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId, boolean viewOrigin) {
        return createTaskFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, originId, viewOrigin, false);
    }

    /**
     * Creates a new task filter process instance
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createTaskFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId) {
        return createTaskFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, originId, false, false);
    }

    /**
     * Creates a new task filter process instance
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createTaskFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets) {
        return createTaskFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, null, false, false);
    }

    /**
     * Creates a new task filter process instance
     *
     * @param title                 unique title of the default filter
     * @param icon                  material icon identifier of the default filter
     * @param filterVisibility      filter visibility
     * @param filterQuery           the elastic query string query used by the filter
     * @param allowedNets           list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata        metadata of the serialised filter as generated by the frontend
     * @param titleTranslations     a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories whether the default search categories should be merged with the search categories specified in the metadata
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createTaskFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories) {
        return createTaskFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, true, null, false, false);
    }

    /**
     * Creates a new task filter process instance
     *
     * @param title             unique title of the default filter
     * @param icon              material icon identifier of the default filter
     * @param filterVisibility  filter visibility
     * @param filterQuery       the elastic query string query used by the filter
     * @param allowedNets       list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata    metadata of the serialised filter as generated by the frontend
     * @param titleTranslations a map of locale codes to translated strings for the filter title
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createTaskFilter(String title, String icon, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations) {
        return createTaskFilter(title, icon, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, true, true, null, false, false);
    }

    /**
     * Creates a new filter process instance of the provided type
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterType             the type of the filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @param viewOrigin             true if the parent was a view. false if the parent was another filter
     * @param isImported             whether the filter is being created by importing it from na XML file
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createFilter(String title, String icon, String filterType, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId, boolean viewOrigin, boolean isImported) {
        filterMetadata.put("filterType", filterType);
        filterMetadata.put("defaultSearchCategories", withDefaultCategories);
        filterMetadata.put("inheritAllowedNets", inheritBaseAllowedNets);
        return createFilterCase(title, icon, filterType, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, originId, viewOrigin, isImported);
    }

    /**
     * Creates a new filter process instance of the provided type
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterType             the type of the filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @param viewOrigin             true if the parent was a view. false if the parent was another filter
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createFilter(String title, String icon, String filterType, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId, boolean viewOrigin) {
        return createFilter(title, icon, filterType, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, originId, viewOrigin, false);
    }

    /**
     * Creates a new filter process instance of the provided type
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterType             the type of the filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @param originId               the ID of the parent if any
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createFilter(String title, String icon, String filterType, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets, String originId) {
        return createFilter(title, icon, filterType, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, originId, false, false);
    }

    /**
     * Creates a new filter process instance of the provided type
     *
     * @param title                  unique title of the default filter
     * @param icon                   material icon identifier of the default filter
     * @param filterType             the type of the filter
     * @param filterVisibility       filter visibility
     * @param filterQuery            the elastic query string query used by the filter
     * @param allowedNets            list of process identifiers allowed for search categories metadata generation
     * @param filterMetadata         metadata of the serialised filter as generated by the frontend
     * @param titleTranslations      a map of locale codes to translated strings for the filter title
     * @param withDefaultCategories  whether the default search categories should be merged with the search categories specified in the metadata
     * @param inheritBaseAllowedNets whether the base allowed nets should be merged with the allowed nets specified in the filter field
     * @return an empty Optional if the filter process does not exist. An existing filter process instance if a filter process instance with the same name already exists. A new filter process instance if not.
     */
    public Optional<Case> createFilter(String title, String icon, String filterType, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, boolean withDefaultCategories, boolean inheritBaseAllowedNets) {
        return createFilter(title, icon, filterType, filterVisibility, filterQuery, allowedNets, filterMetadata, titleTranslations, withDefaultCategories, inheritBaseAllowedNets, null, false, false);
    }

    private Optional<Case> createFilterCase(String title, String icon, String filterType, String filterVisibility, String filterQuery, List<String> allowedNets, Map<String, Object> filterMetadata, Map<String, String> titleTranslations, String originId, boolean viewOrigin, boolean isImported) {
        PetriNet filterNet = this.petriNetService.getNewestVersionByIdentifier("filter");
        if (filterNet == null) {
            return Optional.empty();
        }

        IUser loggedUser = this.userService.getLoggedOrSystem();
        if (loggedUser.getStringId().equals(this.userService.getSystem().getStringId())) {
            Case filterCase = this.workflowService.searchOne(QCase.case$.processIdentifier.eq("filter").and(QCase.case$.title.eq(title)).and(QCase.case$.author.id.eq(userService.getSystem().getStringId())));
            if (filterCase != null) {
                return Optional.of(filterCase);
            }
        }

        try {
            Case filterCase = this.workflowService.createCase(filterNet.getStringId(), title, null, (LoggedUser) userService.transformToLoggedUser(loggedUser)).getCase();
            filterCase.setIcon(icon);
            filterCase = this.workflowService.save(filterCase);
            Task newFilterTask = this.taskService.searchOne(QTask.task.transitionId.eq(AUTO_CREATE_TRANSITION).and(QTask.task.caseId.eq(filterCase.getStringId())));
            this.taskService.assignTask(newFilterTask, this.userService.getLoggedOrSystem());

            Map<String, Map<String, Object>> setDataMap = new LinkedHashMap<>();
            setDataMap.put(FILTER_TYPE_FIELD_ID, Map.of(
                    "type", "enumeration_map",
                    "value", filterType
            ));
            setDataMap.put(FILTER_VISIBILITY_FIELD_ID, Map.of(
                    "type", "enumeration_map",
                    "value", filterVisibility
            ));
            setDataMap.put(FILTER_FIELD_ID, Map.of(
                    "type", "filter",
                    "value", filterQuery,
                    "allowedNets", allowedNets,
                    "filterMetadata", filterMetadata // TODO this is a map of <String, Object> that needs to be converted to string
            ));

            if (originId != null) {
                setDataMap.put(viewOrigin ? FILTER_ORIGIN_VIEW_ID_FIELD_ID : FILTER_PARENT_CASE_ID_FIELD_ID, Map.of(
                        "type", "text",
                        "value", originId
                ));
            }

            this.dataService.setData(newFilterTask, ImportHelper.populateDatasetWithObject(setDataMap));
            if (isImported) {
                this.dataService.setData(newFilterTask, ImportHelper.populateDataset(Map.of(
                        IS_IMPORTED, Map.of(
                                "type", "number",
                                "value", "1"
                        )
                )));
            }

            final I18nString translatedTitle = new I18nString(title);
            titleTranslations.forEach(translatedTitle::addTranslation);

            filterCase = this.workflowService.findOne(filterCase.getStringId());
            filterCase.getDataSet().get(FILTER_I18N_TITLE_FIELD_ID).setValue(translatedTitle);
            workflowService.save(filterCase);

            this.taskService.finishTask(newFilterTask, this.userService.getLoggedOrSystem());
            return Optional.of(this.workflowService.findOne(filterCase.getStringId()));
        } catch (Exception ex) {
            log.error("Failed to create filter case", ex);
            return Optional.empty();
        }
    }

}
