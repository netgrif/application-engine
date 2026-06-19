package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.AsyncRunner
import com.netgrif.application.engine.auth.domain.Author
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.LoggedUser
import com.netgrif.application.engine.auth.service.UserDetailsServiceImpl
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest
import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.configuration.PublicViewProperties
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.application.engine.export.configuration.ExportConfiguration
import com.netgrif.application.engine.export.domain.ExportDataConfig
import com.netgrif.application.engine.export.service.interfaces.IExportService
import com.netgrif.application.engine.files.IStorageResolverService
import com.netgrif.application.engine.files.StorageResolverService
import com.netgrif.application.engine.files.interfaces.IStorageService
import com.netgrif.application.engine.history.service.IHistoryService
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService
import com.netgrif.application.engine.importer.service.FieldFactory
import com.netgrif.application.engine.mail.domain.MailDraft
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService
import com.netgrif.application.engine.mail.interfaces.IMailService
import com.netgrif.application.engine.menu.domain.FilterBody
import com.netgrif.application.engine.menu.domain.MenuItemBody
import com.netgrif.application.engine.menu.domain.MenuItemConstants
import com.netgrif.application.engine.menu.domain.configurations.CaseViewBody
import com.netgrif.application.engine.menu.domain.configurations.TaskViewBody
import com.netgrif.application.engine.menu.domain.configurations.ViewBody
import com.netgrif.application.engine.menu.domain.dashboard.DashboardItemBody
import com.netgrif.application.engine.menu.domain.dashboard.DashboardManagementBody
import com.netgrif.application.engine.menu.service.interfaces.DashboardItemService
import com.netgrif.application.engine.menu.service.interfaces.DashboardManagementService
import com.netgrif.application.engine.menu.service.interfaces.IMenuItemService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.pdf.generator.config.PdfResource
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfGenerator
import com.netgrif.application.engine.petrinet.domain.*
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.DynamicValidation
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.version.Version
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.pfql.service.IResourceSearchService
import com.netgrif.application.engine.pfql.service.ISearchService
import com.netgrif.application.engine.rules.domain.RuleRepository
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.*
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome
import com.netgrif.application.engine.workflow.service.FileFieldInputStream
import com.netgrif.application.engine.workflow.service.TaskService
import com.netgrif.application.engine.workflow.service.interfaces.*
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference
import com.querydsl.core.types.Predicate
import groovy.transform.NamedVariant
import org.bson.types.ObjectId
import org.quartz.Scheduler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

import java.nio.file.Files
import java.time.ZoneId
import java.util.stream.Collectors
/**
 * ActionDelegate class contains Actions API methods.
 */
@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class ActionDelegate {

    static final Logger log = LoggerFactory.getLogger(ActionDelegate)

    private static final String FILTER_FIELD_I18N_FILTER_NAME = "i18n_filter_name"

    static final String UNCHANGED_VALUE = "unchangedooo"
    static final String ALWAYS_GENERATE = "always"
    static final String ONCE_GENERATE = "once"
    static final String TRANSITIONS = "transitions"
    public static final String GLOBAL_ROLE = "GLOBAL_ROLE"

    @Value('${nae.mail.from}')
    private String mailFrom

    @Value('${nae.create.default.filters:false}')
    private Boolean createDefaultFilters

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
    IPdfGenerator pdfGenerator

    @Autowired
    IMailService mailService

    @Autowired
    INextGroupService nextGroupService

    @Autowired
    IProcessRoleService processRoleService

    @Autowired
    IRegistrationService registrationService

    @Autowired
    IMailAttemptService mailAttemptService

    @Autowired
    UserDetailsServiceImpl userDetailsService

    @Autowired
    IDataValidationExpressionEvaluator dataValidationExpressionEvaluator

    @Autowired
    IInitValueExpressionEvaluator initValueExpressionEvaluator

    @Autowired
    RuleRepository ruleRepository

    @Autowired
    Scheduler scheduler

    @Autowired
    IConfigurableMenuService configurableMenuService

    @Autowired
    IExportService exportService

    @Autowired
    IElasticCaseService elasticCaseService

    @Autowired
    IElasticTaskService elasticTaskService

    @Autowired
    ExportConfiguration exportConfiguration

    @Autowired
    IUriService uriService

    @Autowired
    IImpersonationService impersonationService

    @Autowired
    IHistoryService historyService

    @Autowired
    PublicViewProperties publicViewProperties

    @Autowired
    IMenuItemService menuItemService

    @Autowired
    DashboardManagementService dashboardManagementService

    @Autowired
    DashboardItemService dashboardItemService

    @Autowired
    IStorageResolverService storageResolver

    @Autowired
    ISearchService searchService

    @Autowired
    IResourceSearchService<Case> caseSearchService

    @Autowired
    IResourceSearchService<Task> taskSearchService

    @Autowired
    IResourceSearchService<PetriNet> processSearchService

    @Autowired
    IResourceSearchService<IUser> userSearchService

    FrontendActionOutcome Frontend

    /**
     * Reference of case and task in which current action is taking place.
     */
    Case useCase
    Optional<Task> task
    Map<String, String> params
    def map = [:]
    Action action
    FieldActionsRunner actionsRunner
    List<EventOutcome> outcomes

    def init(Action action, Case useCase, Optional<Task> task, FieldActionsRunner actionsRunner, Map<String, String> params = [:]) {
        this.action = action
        this.useCase = useCase
        this.task = task
        this.params = params
        this.actionsRunner = actionsRunner
        this.initFieldsMap(action.fieldIds)
        this.initTransitionsMap(action.transitionIds)
        this.outcomes = new ArrayList<>()
        this.Frontend = new FrontendActionOutcome(this.useCase, this.task, this.outcomes)
    }

    def initFieldsMap(Map<String, String> fieldIds) {
        fieldIds.each { name, id ->
            set(name, fieldFactory.buildFieldWithoutValidation(useCase, id, null))
        }
    }

    def initTransitionsMap(Map<String, String> transitionIds) {
        transitionIds.each { name, id ->
            set(name, useCase.petriNet.transitions[id])
        }
    }

    def copyBehavior(Field field, Transition transition, Case useCase = this.useCase) {
        if (!useCase.hasFieldBehavior(field.stringId, transition.stringId)) {
            useCase.dataSet.get(field.stringId).addBehavior(transition.stringId, transition.dataSet.get(field.stringId).behavior)
        }
    }

    def visible = { Field field, Transition trans, Case useCase = this.useCase ->
        copyBehavior(field, trans, useCase)
        useCase.dataSet.get(field.stringId).makeVisible(trans.stringId)
    }

    def editable = { Field field, Transition trans, Case useCase = this.useCase ->
        copyBehavior(field, trans, useCase)
        useCase.dataSet.get(field.stringId).makeEditable(trans.stringId)
    }

    def required = { Field field, Transition trans, Case useCase = this.useCase ->
        copyBehavior(field, trans, useCase)
        useCase.dataSet.get(field.stringId).makeRequired(trans.stringId)
    }

    def optional = { Field field, Transition trans, Case useCase = this.useCase ->
        copyBehavior(field, trans, useCase)
        useCase.dataSet.get(field.stringId).makeOptional(trans.stringId)
    }

    def hidden = { Field field, Transition trans, Case useCase = this.useCase ->
        copyBehavior(field, trans, useCase)
        useCase.dataSet.get(field.stringId).makeHidden(trans.stringId)
    }

    def forbidden = { Field field, Transition trans, Case useCase = this.useCase ->
        copyBehavior(field, trans, useCase)
        useCase.dataSet.get(field.stringId).makeForbidden(trans.stringId)
    }

    def initial = { Field field, Transition trans, Case useCase = this.useCase ->
        useCase.petriNet.transitions.get(trans.stringId).dataSet.get(field.stringId).behavior
    }

    def unchanged = { return UNCHANGED_VALUE }

    def initValueOfField = { Field field, Map<String, String> params = [:] ->
        if (!field.hasDefault()) {
            return null
        } else if (field.isDynamicDefaultValue()) {
            return initValueExpressionEvaluator.evaluate(useCase, field, params)
        }
        return field.defaultValue
    }

    def transitions = { return TRANSITIONS }

    def getInit() {
        return initValueOfField
    }

    def init(Field field) {
        return initValueOfField(field)
    }

    /**
     * Changes behavior of a given field on given transition (transitions) or on each transition containing a field if certain condition is being met.
     * <br>
     * Example 1:
     * <pre>
     *     condition: f.conditionId,
     *     text: f.textId,
     *     transition: t.transitionId;
     *
     *     make text, visible on transition when { condition.value == true }
     * </pre>
     * This code will change the field <i>text</i> behaviour to <i>visible</i> on given transition when field's <i>condition</i> value is equal to <i>true</i>.
     *
     * Example 2:
     * <pre>
     *     condition: f.conditionId,
     *     text: f.textId,
     *     transition: t.transitionId,
     *     anotherTransition: t.anotherTransitionId;
     *
     *     make text, visible on ([transition, anotherTransition]) when { condition.value == true }
     * </pre>
     * This code will change the field <i>text</i> behaviour to <i>visible</i> on given transitions when field's <i>condition</i> value is equal to <i>true</i>.
     *
     * Example 3:
     * <pre>
     *     condition: f.conditionId,
     *     text: f.textId;
     *
     *     make text, visible on transitions when { condition.value == true }
     * </pre>
     *
     * This code will change the field <i>text</i> behaviour to <i>visible</i> on each transition that contains the field <i>text</i> when field's <i>condition</i> value is equal to <i>true</i>.
     * @param field which behaviour will be changed
     * @param behavior one of initial, visible, editable, required, optional, hidden, forbidden
     */
    def make(Field field, Closure behavior) {
        def behaviorClosureResult

        [on: { Object transitionObject ->
            [when: { Closure condition ->
                if (condition()) {
                    if (transitionObject instanceof Transition) {
                        behaviorClosureResult = behavior(field, transitionObject)
                        saveFieldBehavior(field, transitionObject, (behavior == initial) ? behaviorClosureResult as Set : null)
                    } else if (transitionObject instanceof List<?>) {
                        transitionObject.each { trans ->
                            if (trans instanceof Transition) {
                                if (trans.dataSet.containsKey(field.stringId)) {
                                    behaviorClosureResult = behavior(field, trans)
                                    saveFieldBehavior(field, trans, (behavior == initial) ? behaviorClosureResult as Set : null)
                                }
                            } else if (trans instanceof Task) {
                                saveFieldBehaviorWithTask(field, trans, behavior, behaviorClosureResult)
                            } else {
                                throw new IllegalArgumentException("Invalid call of make method. Method call should contain a list of transitions.")
                            }
                        }
                    } else if (transitionObject instanceof Closure) {
                        if (transitionObject == transitions) {
                            useCase.petriNet.transitions.each { transitionEntry ->
                                Transition trans = transitionEntry.value
                                if (trans.dataSet.containsKey(field.stringId)) {
                                    behaviorClosureResult = behavior(field, trans)
                                    saveFieldBehavior(field, trans, (behavior == initial) ? behaviorClosureResult as Set : null)
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("Invalid call of make method. Method call should contain specific transition (transitions) or keyword \'transitions\'.")
                        }
                    } else if (transitionObject instanceof Task) {
                        saveFieldBehaviorWithTask(field, transitionObject, behavior, behaviorClosureResult)
                    } else {
                        throw new IllegalArgumentException("Invalid call of make method. Method call should contain specific transition (transitions) or keyword \'transitions\'.")
                    }
                }
            }]
        }]
    }

    /**
     * Changes behavior of given fields on given transition (transitions) or on each transition containing given fields if certain condition is being met.
     * <br>
     * Example 1:
     * <pre>
     *     condition: f.conditionId,
     *     text: f.textId,
     *     anotherText: f.anotherTextId,
     *     transition: t.transitionId;
     *
     *     make [text, anotherText], visible on transition when { condition.value == true }
     * </pre>
     * This code will change the behavior of fields <i>text</i> and <i>anotherText</i> to <i>visible</i> on given transition when field's <i>condition</i> value is equal to <i>true</i>.
     *
     * Example 2:
     * <pre>
     *     condition: f.conditionId,
     *     text: f.textId,
     *     anotherText: f.anotherTextId,
     *     transition: t.transitionId,
     *     anotherTransition: t.anotherTransitionId;
     *
     *     make [text, anotherText], visible on ([transition, anotherTransition]) when { condition.value == true }
     * </pre>
     * This code will change the behavior of fields <i>text</i> and <i>anotherText</i> to <i>visible</i> on given transition when field's <i>condition</i> value is equal to <i>true</i>.
     *
     * Example 3:
     * <pre>
     *     condition: f.conditionId,
     *     text: f.textId,
     *     anotherText: f.anotherTextId;
     *
     *     make [text, anotherText], visible on transitions when { condition.value == true }
     * </pre>
     *
     * Example 4:
     * <pre>
     *     taskRef: f.taskRef_0;
     *     def taskIds = [taskRef.value[0]] as List
     *     make ["referenced_text"], editable on taskIds when { true }
     * </pre>
     *
     * Example 5:
     * <pre>
     *     taskRef: f.taskRef_0;
     *     def tasks = [taskService.findOne(taskRef_0.value[0])] as List
     *     def field = getFieldOfTask(tasks[0].stringId, "referenced_text")
     *     make [field], editable on tasks when { true }
     * </pre>
     * @param list of fields which behaviour will be changed
     * @param behavior one of initial, visible, editable, required, optional, hidden, forbidden
     */
    def make(List<?> fields, Closure behavior) {
        def behaviorClosureResult

        [on: { Object transitionObject ->
            [when: { Closure condition ->
                if (condition()) {
                    if (transitionObject instanceof Transition) {
                        fields.forEach { field ->
                            behaviorClosureResult = behavior(field, transitionObject)
                            saveFieldBehavior(field as Field, transitionObject, (behavior == initial) ? behaviorClosureResult as Set : null)
                        }
                    } else if (transitionObject instanceof List<?>) {
                        transitionObject.each { trans ->
                            if (trans instanceof Transition) {
                                fields.each { field ->
                                    if (trans.dataSet.containsKey(field.stringId)) {
                                        behaviorClosureResult = behavior(field, trans)
                                        saveFieldBehavior(field as Field, trans, (behavior == initial) ? behaviorClosureResult as Set : null)
                                    }
                                }
                            } else if (trans instanceof Task) {
                                fields.forEach { field ->
                                    saveFieldBehaviorWithTask(field as Field<?>, trans, behavior, behaviorClosureResult)
                                }
                            } else if (trans instanceof String) {
                                fields.each { fieldId ->
                                    if (fieldId instanceof String) {
                                        Task task = findTask(trans as String)
                                        Field<?> field = getFieldOfTask(trans as String, fieldId as String)
                                        saveFieldBehaviorWithTask(field, task, behavior, behaviorClosureResult)
                                    } else {
                                        throw new IllegalArgumentException("Invalid call of make method. If 'on' attribute represents list of task IDs, then field attribute should represent field IDs.")
                                    }
                                }
                            } else {
                                throw new IllegalArgumentException("Invalid call of make method. Method call should contain a list of transitions.")
                            }
                        }
                    } else if (transitionObject instanceof Closure) {
                        if (transitionObject == transitions) {
                            useCase.petriNet.transitions.each { transitionEntry ->
                                Transition trans = transitionEntry.value
                                fields.each { field ->
                                    if (trans.dataSet.containsKey(field.stringId)) {
                                        behaviorClosureResult = behavior(field, trans)
                                        saveFieldBehavior(field as Field, trans, (behavior == initial) ? behaviorClosureResult as Set : null)
                                    }
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("Invalid call of make method. Method call should contain specific transition (transitions) or keyword \'transitions\'.")
                        }
                    } else if (transitionObject instanceof Task) {
                        fields.forEach { field ->
                            saveFieldBehaviorWithTask(field, transitionObject, behavior, behaviorClosureResult)
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid call of make method. Method call should contain specific transition (transitions) or keyword \'transitions\'.")
                    }
                }
            }]
        }]
    }

    protected void saveFieldBehaviorWithTask(Field<?> field, Task task, Closure behavior, def behaviorClosureResult) {
        Case aCase = workflowService.findOne(task.caseId)
        Transition transition = aCase.getPetriNet().getTransition(task.getTransitionId())
        behaviorClosureResult = behavior(field, transition, aCase)
        saveFieldBehavior(field, transition, (behavior == initial) ? behaviorClosureResult as Set : null, aCase, Optional.of(task))
    }

    protected SetDataEventOutcome createSetDataEventOutcome(Case useCase = this.useCase, Optional<Task> task = this.task) {
        return new SetDataEventOutcome(useCase, task.orElse(null))
    }

    def saveFieldBehavior(Field field, Transition trans, Set<FieldBehavior> initialBehavior, Case targetCase = this.useCase, Optional<Task> targetTask = this.task) {
        Map<String, Set<FieldBehavior>> fieldBehavior = targetCase.dataSet.get(field.stringId).behavior
        if (initialBehavior != null)
            fieldBehavior.put(trans.stringId, initialBehavior)
        saveTargetCase(targetCase)
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("type", field.type.name)
        changedField.addBehavior(fieldBehavior)
        SetDataEventOutcome outcome = createSetDataEventOutcome(targetCase, targetTask)
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedChoices(ChoiceField field, Case targetCase = this.useCase, Optional<Task> targetTask = this.task) {
        targetCase.dataSet.get(field.stringId).choices = field.choices
        saveTargetCase(targetCase)
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("choices", field.choices.collect { it.getTranslation(LocaleContextHolder.locale) })
        SetDataEventOutcome outcome = createSetDataEventOutcome(targetCase, targetTask)
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedAllowedNets(CaseField field, Case targetCase = this.useCase, Optional<Task> targetTask = this.task) {
        targetCase.dataSet.get(field.stringId).allowedNets = field.allowedNets
        saveTargetCase(targetCase)
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("allowedNets", field.allowedNets)
        SetDataEventOutcome outcome = createSetDataEventOutcome(targetCase, targetTask)
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedOptions(MapOptionsField field, Case targetCase = this.useCase, Optional<Task> targetTask = this.task) {
        targetCase.dataSet.get(field.stringId).options = field.options
        saveTargetCase(targetCase)
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("options", field.options.collectEntries { key, value -> [key, (value as I18nString).getTranslation(LocaleContextHolder.locale)] })
        SetDataEventOutcome outcome = createSetDataEventOutcome(targetCase, targetTask)
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedValidation(Field field, Case targetCase = this.useCase, Optional<Task> targetTask = this.task) {
        targetCase.dataSet.get(field.stringId).validations = field.validations
        saveTargetCase(targetCase)
        List<Validation> compiled = field.validations.collect { it.clone() }
        compiled.findAll { it instanceof DynamicValidation }.collect { (DynamicValidation) it }.each {
            it.compiledRule = dataValidationExpressionEvaluator.compile(targetCase, it.expression)
        }
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("validations", compiled.collect { it.getLocalizedValidation(LocaleContextHolder.locale) })
        SetDataEventOutcome outcome = createSetDataEventOutcome(targetCase, targetTask)
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def close = { Transition[] transitions ->
        def service = ApplicationContextProvider.getBean("taskService")
        if (!service) {
            log.error("Could not find task service")
            return
        }

        def transitionIds = transitions.collect { it.stringId } as Set
        service.cancelTasksWithoutReload(transitionIds, useCase.stringId)
    }

    def execute(String taskId) {
        [with : { Map dataSet ->
            executeTasks(dataSet, taskId, { it._id.isNotNull() })
        },
         where: { Closure<Predicate> closure ->
             [with: { Map dataSet ->
                 executeTasks(dataSet, taskId, closure)
             }]
         }]
    }

    def execute(Task task) {
        [with : { Map dataSet ->
            executeTasks(dataSet, task.stringId, { it._id.isNotNull() })
        },
         where: { Closure<Predicate> closure ->
             [with: { Map dataSet ->
                 executeTasks(dataSet, task.stringId, closure)
             }]
         }]
    }

    void executeTasks(Map dataSet, String taskId, Closure<Predicate> predicateClosure) {
        List<String> caseIds = searchCases(predicateClosure)
        QTask qTask = new QTask("task")
        Page<Task> tasksPage = taskService.searchAll(qTask.transitionId.eq(taskId).and(qTask.caseId.in(caseIds)))
        tasksPage?.content?.each { task ->
            addTaskOutcomes(task, dataSet)
        }
    }

    void executeTask(String transitionId, Map dataSet) {
        QTask qTask = new QTask("task")
        Task task = taskService.searchOne(qTask.transitionId.eq(transitionId).and(qTask.caseId.eq(useCase.stringId)))
        addTaskOutcomes(task, dataSet)
    }

    private addTaskOutcomes(Task task, Map dataSet) {
        this.outcomes.add(taskService.assignTask(task.stringId))
        this.outcomes.add(dataService.setData(task.stringId, ImportHelper.populateDataset(dataSet as Map<String, Map<String, String>>)))
        this.outcomes.add(taskService.finishTask(task.stringId))
    }

    List<String> searchCases(Closure<Predicate> predicates) {
        QCase qCase = new QCase("case")
        def expression = predicates(qCase)
        Page<Case> page = workflowService.searchAll(expression)

        return page.content.collect { it.stringId }
    }

    def change(String fieldId, String caseId, String taskId = null) {
        Case targetCase
        if (caseId == null) {
            throw new IllegalArgumentException("Case ID cannot be null when setting data between processes.")
        }
        targetCase = workflowService.findOne(caseId)
        Task targetTask = null
        if (taskId != null) {
            targetTask = taskService.findOne(taskId)
        }
        Field field = targetCase.getPetriNet().getDataSet().get(fieldId)
        change(field, targetCase, Optional.of(targetTask))
    }

    def change(Field field, Case targetCase = this.useCase, Optional<Task> targetTask = this.task) {
        [about              : { cl -> // TODO: deprecated
            changeFieldValue(field, cl, targetCase, targetTask)
        },
         value              : { cl ->
             changeFieldValue(field, cl, targetCase, targetTask)
         },
         choices            : { cl ->
             if (!(field instanceof MultichoiceField || field instanceof EnumerationField))
                 return

             def values = cl()
             if (values == null || (values instanceof Closure && values() == UNCHANGED_VALUE))
                 return


             if (!(values instanceof Collection))
                 values = [values]
             field = (ChoiceField) field
             if (values.every { it instanceof I18nString }) {
                 field.setChoices(values as Set<I18nString>)
             } else {
                 field.setChoicesFromStrings(values as Set<String>)
             }
             saveChangedChoices(field, targetCase, targetTask)
         },
         allowedNets        : { cl ->
             if (!(field instanceof CaseField))
                 return

             def allowedNets = cl()
             if (allowedNets instanceof Closure && allowedNets() == UNCHANGED_VALUE)
                 return

             field = (CaseField) field
             if (allowedNets == null) {
                 field.setAllowedNets(new ArrayList<String>())
             } else if (allowedNets instanceof List) {
                 field.setAllowedNets(allowedNets)
             } else {
                 return
             }
             saveChangedAllowedNets(field, targetCase, targetTask)
         },
         options            : { cl ->
             if (!(field instanceof MultichoiceMapField || field instanceof EnumerationMapField
                     || field instanceof MultichoiceField || field instanceof EnumerationField))
                 return

             def options = cl()
             if (options == null || (options instanceof Closure && options() == UNCHANGED_VALUE))
                 return
             if (!(options instanceof Map && options.every { it.getKey() instanceof String }))
                 return

             if (field instanceof MapOptionsField) {
                 field = (MapOptionsField) field
                 if (options.every { it.getValue() instanceof I18nString }) {
                     field.setOptions(options)
                 } else {
                     Map<String, I18nString> newOptions = new LinkedHashMap<>()
                     options.each { it -> newOptions.put(it.getKey() as String, new I18nString(it.getValue() as String)) }
                     field.setOptions(newOptions)
                 }
                 saveChangedOptions(field, targetCase, targetTask)
             } else if (field instanceof ChoiceField) {
                 field = (ChoiceField) field
                 if (options.every { it.getValue() instanceof I18nString }) {
                     Set<I18nString> choices = new LinkedHashSet<>()
                     options.forEach({ k, v -> choices.add(v) })
                     field.setChoices(choices)
                 } else {
                     Set<I18nString> newChoices = new LinkedHashSet<>()
                     options.each { it -> newChoices.add(new I18nString(it.getValue() as String)) }
                     field.setChoices(newChoices)
                 }
                 saveChangedChoices(field, targetCase, targetTask)
             }

         },
         validations        : { cl ->
             changeFieldValidations(field, cl, targetCase, targetTask)
         },
         componentProperties: { cl ->
             def properties = cl()
             if (properties == null || (properties instanceof Closure && properties() == UNCHANGED_VALUE)) {
                 return
             }
             if (!(properties instanceof Map && properties.every { it.getKey() instanceof String })) {
                 return
             }

             addSetDataOutcomeToOutcomes(dataService.changeComponentProperties(targetCase, targetTask.get(), field.stringId, properties))
         }
        ]
    }

    void changeFieldValue(Field field, def cl, Case targetCase = this.useCase, Optional<Task> targetTask = this.task) {
        def value = cl()
        if (value instanceof Closure) {
            if (value == initValueOfField) {
                value = initValueOfField(field)

            } else if (value() == UNCHANGED_VALUE) {
                return
            }
        }
        if (value == null && targetCase.dataSet.get(field.stringId).value != null) {
            if (field instanceof FileListField && targetTask.isPresent()) {
                field.value.namesPaths.forEach(namePath -> {
                    dataService.deleteFileByName(targetTask.get().stringId, field.stringId, namePath.name)
                })
            }
            if (field instanceof FileField && targetTask.isPresent()) {
                dataService.deleteFile(targetTask.get().stringId, field.stringId)
            }
            field.clearValue()
            saveChangedValue(field, targetCase)
        }
        if (value != null) {
            if (field instanceof CaseField) {
                value = ((List) value).stream().map({ entry -> entry instanceof Case ? entry.getStringId() : entry }).collect(Collectors.toList())
                dataService.validateCaseRefValue((List<String>) value, ((CaseField) field).getAllowedNets())
            }
            if (field instanceof NumberField) {
                value = value as Double
            }
            if (field instanceof UserListField && (value instanceof String[] || value instanceof List)) {
                LinkedHashSet<UserFieldValue> users = new LinkedHashSet<>()
                value.each { id -> users.add(new UserFieldValue(userService.findById(id as String, false))) }
                value = new UserListFieldValue(users)
            }
//            if (field instanceof TaskField && targetTask.isPresent()) {
//                dataService.validateTaskRefValue(value, targetTask.get().getStringId());
//            }
            field.value = value
            saveChangedValue(field, targetCase)
        }

        targetCase = dataService.applyFieldConnectedChanges(targetCase, field)
        ChangedField changedField = new ChangedField(field.stringId)
        if (field instanceof I18nField) {
            changedField.attributes.put("value", value)
        } else {
            changedField.addAttribute("value", value)
        }
        changedField.addAttribute("type", field.type.name)
        SetDataEventOutcome outcome = createSetDataEventOutcome(targetCase, targetTask)
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveTargetCase(Case targetCase) {
        if (targetCase != useCase) {
            workflowService.save(targetCase)
        }
    }

    def saveChangedValue(Field field, Case targetCase = useCase) {
        targetCase.dataSet.get(field.stringId).value = field.value
        saveTargetCase(targetCase)
    }

    void changeFieldValidations(Field field, def cl, Case targetCase = this.useCase, Optional<Task> targetTask = this.task) {
        def valid = cl()
        if (valid == UNCHANGED_VALUE)
            return
        List<Validation> newValidations = []
        if (valid != null) {
            if (valid instanceof String) {
                newValidations = [new Validation(valid as String)]
            } else if (valid instanceof Validation) {
                newValidations = [valid]
            } else if (valid instanceof Collection) {
                if (valid.every { it instanceof Validation }) {
                    newValidations = valid
                } else {
                    newValidations = valid.collect { new Validation(it as String) }
                }
            }
        }
        field.validations = newValidations
        saveChangedValidation(field, targetCase, targetTask)
    }

    def always = { return ALWAYS_GENERATE }
    def once = { return ONCE_GENERATE }

    def generate(String methods, Closure repeated) {
        [into: { Field field ->
            if (field.type == FieldType.FILE)
                File f = new FileGenerateReflection(useCase, field as FileField, repeated() == ALWAYS_GENERATE).callMethod(methods) as File
            else if (field.type == FieldType.TEXT)
                new TextGenerateReflection(useCase, field as TextField, repeated() == ALWAYS_GENERATE).callMethod(methods) as String
            /*if(f != null) {
                useCase.dataSet.get(field.objectId).value = f.name
                field.value = f.name
            }*/
        }]
    }

    def changeCaseProperty(String property) {
        [about: { cl ->
            def value = cl()
            if (value instanceof Closure && value() == UNCHANGED_VALUE) return
            useCase."$property" = value

            if (property == "title" || property == "color") {
                List<Task> tasks = taskService.findAllByCase(useCase.stringId)

                tasks.each { task ->
                    task."case${property.capitalize()}" = value
                }
                taskService.save(tasks)
            }
        }]
    }

    //Cache manipulation
    def cache(String name, Object value) {
        actionsRunner.addToCache("${useCase.stringId}-${name}", value)
    }

    def cache(String name) {
        return actionsRunner.getFromCache("${useCase.stringId}-${name}" as String)
    }

    def cacheFree(String name) {
        actionsRunner.removeFromCache("${useCase.stringId}-${name}")
    }

    //Get PSC - DSL only for Insurance
    def byCode = { String code ->
        return actionsRunner.postalCodeService.findAllByCode(code)
    }

    def byCity = { String city ->
        return actionsRunner.postalCodeService.findAllByCity(city)
    }

    def psc(Closure find, String input) {
        if (find)
            return find(input)
        return null
    }

    def byIco = { String ico ->
        return actionsRunner.orsrService.findByIco(ico)
    }

    def orsr(Closure find, String ico) {
        return find?.call(ico)
    }

    Object get(String key) { map[key] }

    void set(String key, Object value) { map[key] = value }

    List<Case> findCases(Closure<Predicate> predicate) {
        QCase qCase = new QCase("case")
        Page<Case> result = workflowService.searchAll(predicate(qCase))
        return result.content
    }

    List<Case> findCases(Closure<Predicate> predicate, Pageable pageable) {
        QCase qCase = new QCase("case")
        Page<Case> result = workflowService.search(predicate(qCase), pageable)
        return result.content
    }

    /**
     * Finds cases referenced by a field in its value.
     *
     * Use this overload when working on a case from the current action context. For working with fields from out of the
     * current action context see other overloads of this action.
     *
     * <p>If the field value is {@code null}, this method returns an empty list.</p>
     * <p>If the value cannot be converted to case IDs, this method returns an empty list.</p>
     *
     * @param caseRef field whose value contains case IDs, may be of types
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#CASE_REF},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#ENUMERATION_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#STRING_COLLECTION},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TEXT},
     * @return list of matching cases, or an empty list when the field value is {@code null}
     * @see ActionDelegate#findCases(DataField)
     * @see ActionDelegate#findCases(List)
     * @see ActionDelegate#findCases(Closure)
     * @see ActionDelegate#findCases(Closure, Pageable)
     */
    List<Case> findCases(Field caseRef) {
        if(caseRef.value == null) {
            log.debug("[findCases(Field)]: Value of field with id [${caseRef.importId}] is null, returning empty list.")
            return []
        }
        try {
            return this.findCases([caseRef.value].flatten() as List<String>)
        } catch (ClassCastException e) {
            log.error("Method cannot be used with field with id [${caseRef.importId}].", e)
            return []
        }
    }

    /**
     * Finds cases referenced by a dataField in its value.
     *
     * Use this overload when working on a case not from the current action context. For working with fields from the current
     * action context see other overloads of this action.
     *
     * <p>If the field value is {@code null}, this method returns an empty list.</p>
     * <p>If the value cannot be converted to case IDs, this method returns an empty list.</p>
     *
     * @param caseRef field whose value contains case IDs, may be of types
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#CASE_REF},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#ENUMERATION_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#STRING_COLLECTION},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TEXT},
     * @return list of matching cases, or an empty list when the field value is {@code null}
     * @see ActionDelegate#findCases(Field)
     * @see ActionDelegate#findCases(List)
     * @see ActionDelegate#findCases(Closure)
     * @see ActionDelegate#findCases(Closure, Pageable)
     */
    List<Case> findCases(DataField caseRef) {
        if(caseRef.value == null) {
            log.debug("[findCases(DataField]: Value of field is null, returning empty list.")
            return []
        }
        try {
            return this.findCases([caseRef.value].flatten() as List<String>)
        } catch (ClassCastException e) {
            log.error("Method cannot be used with field.", e)
            return []
        }
    }


    /**
     * Finds cases by their MongoDB IDs.
     *
     * @param mongoIds list of case IDs
     * @return list of matching cases, or an empty list when the input is {@code null} or {@code empty}
     * @see ActionDelegate#findCases(Field)
     * @see ActionDelegate#findCases(DataField)
     * @see ActionDelegate#findCases(Closure)
     * @see ActionDelegate#findCases(Closure, Pageable)
     */
    List<Case> findCases(List<String> mongoIds) {
        if(mongoIds == null || mongoIds.empty) {
            log.debug("[findCases(List<String>)]: Null value detected, returning empty list.")
            return []
        }
        return workflowService.findAllById(mongoIds)
    }

    Case findCase(Closure<Predicate> predicate) {
        QCase qCase = new QCase("case")
        return workflowService.searchOne(predicate(qCase))
    }



    /**
     * Finds the first case referenced by a field in its value.
     *
     * Use this overload when working on a case from current action context. For working with fields from out of the
     * current action context see other overloads of this action.
     *
     * <p>If the field value is {@code null}, this method returns {@code null}.</p>
     * <p>If the value cannot be converted to case IDs, this method returns {@code null}.</p>
     *
     * @param caseRef field whose value contains case IDs, may be of types
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#CASE_REF},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#ENUMERATION_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#STRING_COLLECTION},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TEXT},
     * @return referenced case, or {@code null} when the field value is invalid
     * @see ActionDelegate#findCase(DataField)
     * @see ActionDelegate#findCase(String)
     * @see ActionDelegate#findCase(Closure)
     */
    Case findCase(Field caseRef) {
        if(caseRef.value == null) {
            log.debug("[findCase(Field]: Value of field with id [${caseRef.importId}] is null, returning null.")
            return null
        }
        try {
            List<String> castValue = [caseRef.value].flatten() as List<String>
            if(castValue.size() == 0) {
                log.debug("[findCase(Field]: Value of field with id [${caseRef.importId}] does not contain at least one element, returning null.")
                return null
            }
            return this.findCase(castValue[0])
        } catch (ClassCastException e) {
            log.error("Method cannot be used with field with id [${caseRef.importId}].", e)
            return null
        }
    }


    /**
     * Finds the first case referenced by a dataField in its value.
     *
     * Use this overload when working on a case from out of current action context. For working with fields from the current
     * action context see other overloads of this action.
     *
     * <p>If the field value is {@code null}, this method returns {@code null}.</p>
     * <p>If the value cannot be converted to case IDs, this method returns {@code null}.</p>
     *
     * @param caseRef field whose value contains case IDs, may be of types
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#CASE_REF},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#ENUMERATION_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#STRING_COLLECTION},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TEXT},
     * @return referenced case, or {@code null} when the dataField value is invalid
     * @see ActionDelegate#findCase(Field)
     * @see ActionDelegate#findCase(String)
     * @see ActionDelegate#findCase(Closure)
     */
    Case findCase(DataField caseRef) {
        if(caseRef.value == null) {
            log.debug("[findCase(DataField)]: Value of field is null, returning null.")
            return null
        }
        try {
            List<String> castValue = [caseRef.value].flatten() as List<String>
            if(castValue.size() == 0) {
                log.debug("[findCase(DataField)]: Value of field does not contain at least one element, returning null.")
                return null
            }
            return this.findCase(castValue[0])
        } catch (ClassCastException e) {
            log.error("Method cannot be used with field.", e)
            return null
        }
    }

    /**
     * Finds case by its MongoDB ID.
     *
     * @param mongoId case IDs
     * @return resulting case
     * @see ActionDelegate#findCase(Field)
     * @see ActionDelegate#findCase(DataField)
     * @see ActionDelegate#findCase(Closure)
     */
    Case findCase(String mongoId) {
        return workflowService.findOne(mongoId)
    }

    Case createCase(String identifier, String title = null, String color = "", IUser author = userService.loggedOrSystem, Locale locale = LocaleContextHolder.getLocale(), Map<String, String> params = [:]) {
        return workflowService.createCaseByIdentifier(identifier, title, color, author.transformToLoggedUser(), locale, params).getCase()
    }

    Case createCase(PetriNet net, String title = net.defaultCaseName.getTranslation(locale), String color = "", IUser author = userService.loggedOrSystem, Locale locale = LocaleContextHolder.getLocale(), Map<String, String> params = [:]) {
        CreateCaseEventOutcome outcome = workflowService.createCase(net.stringId, title, color, author.transformToLoggedUser(), params)
        this.outcomes.add(outcome)
        return outcome.getCase()
    }

    /**
     * Deletes a case by its MongoDB ID.
     *
     * @param mongoId case identifier
     * @return deleted case, or {@code null} when the input is {@code null}
     */
    Case deleteCase(String mongoId) {
        if(mongoId == null){
            log.debug("[deleteCase(String)]: Null value detected, returning null.")
            return null
        }
        return this.deleteCase(workflowService.findOne(mongoId))
    }

    /**
     * Deletes the provided case.
     *
     * @param toDelete case to delete
     * @return deleted case, or {@code null} when the input is {@code null}
     */
    Case deleteCase(Case toDelete) {
        if(toDelete == null){
            log.debug("[deleteCase(Case)]: Null value detected, returning null.")
            return null
        }
        return workflowService.deleteCase(toDelete).case
    }

    Task assignTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        String taskId = getTaskId(transitionId, aCase)
        return addTaskOutcomeAndReturnTask(taskService.assignTask(user.transformToLoggedUser(), taskId, params))
    }

    Task assignTask(Task task, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return addTaskOutcomeAndReturnTask(taskService.assignTask(task, user, params))
    }

    /**
     * Assigns tasks for all transitions in the provided list and returns the assigned tasks.
     *
     * @param transitionIds transition identifiers whose tasks should be assigned
     * @param aCase case used to resolve the tasks, defaults to the current case
     * @param user user to assign the tasks to, defaults to the logged or system user
     * @param params additional parameters
     * @return assigned tasks
     */
    List<Task> assignTasksByTransitions(List<String> transitionIds, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        List<String> taskIds = getTaskIds(transitionIds, aCase)
        List<Task> tasks = taskService.findAllById(taskIds)
        return assignTasks(tasks, user, params)
    }

    /**
     * Assigns the provided tasks and returns the assigned tasks.
     *
     * @param tasks tasks to assign
     * @param assignee user to assign the tasks to, defaults to the logged or system user
     * @param params additional parameters
     * @return assigned tasks
     */
    List<Task> assignTasks(List<Task> tasks, IUser assignee = userService.loggedOrSystem, Map<String, String> params = [:]) {
        List<AssignTaskEventOutcome> outcomes = taskService.assignTasks(tasks, assignee, params)
        this.outcomes.addAll(outcomes)
        return outcomes.collect { it.task }
    }

    Task cancelTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        String taskId = getTaskId(transitionId, aCase)
        return addTaskOutcomeAndReturnTask(taskService.cancelTask(user.transformToLoggedUser(), taskId, params))
    }

    Task cancelTask(Task task, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return addTaskOutcomeAndReturnTask(taskService.cancelTask(task, user, params))
    }


    /**
     * Cancels tasks for all transitions in the provided list and returns the canceled tasks.
     *
     * @param transitionIds transition identifiers whose tasks should be canceled
     * @param aCase case used to resolve the tasks, defaults to the current case
     * @param user user performing the cancellation, defaults to the logged or system user
     * @param params additional parameters
     * @return canceled tasks
     */
    List<Task> cancelTasksByTransitions(List<String> transitionIds, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        List<String> taskIds = getTaskIds(transitionIds, aCase)
        List<Task> tasks = taskService.findAllById(taskIds)
        return cancelTasks(tasks, user, params)
    }

    /**
     * Cancels the provided tasks and returns the canceled tasks.
     *
     * @param tasks tasks to cancel
     * @param user user performing the cancellation, defaults to the logged or system user
     * @param params additional parameters
     * @return canceled tasks
     */
    List<Task> cancelTasks(List<Task> tasks, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        List<CancelTaskEventOutcome> outcomes = taskService.cancelTasks(tasks, user, params)
        this.outcomes.addAll(outcomes)
        return outcomes.collect { it.task }
    }

    private Task addTaskOutcomeAndReturnTask(TaskEventOutcome outcome) {
        this.outcomes.add(outcome)
        return outcome.getTask()
    }

    Task finishTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        String taskId = getTaskId(transitionId, aCase)
        return addTaskOutcomeAndReturnTask(taskService.finishTask(user.transformToLoggedUser(), taskId, params))
    }

    Task finishTask(Task task, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return addTaskOutcomeAndReturnTask(taskService.finishTask(task, user, params))
    }

    /**
     * Finishes tasks for all transitions in the provided list and returns the finished tasks.
     *
     * @param transitionIds transition identifiers whose tasks should be finished
     * @param aCase case used to resolve the tasks, defaults to the current case
     * @param user user performing the finish operation, defaults to the logged or system user
     * @param params additional parameters
     * @return finished tasks
     */
    List<Task> finishTasksByTransitions(List<String> transitionIds, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        List<String> taskIds = getTaskIds(transitionIds, aCase)
        List<Task> tasks = taskService.findAllById(taskIds)
        return finishTasks(tasks, user, params)
    }

    /**
     * Finishes the provided tasks and returns the finished tasks.
     *
     * @param tasks tasks to finish
     * @param finisher user performing the finish operation, defaults to the logged or system user
     * @param params additional parameters
     * @return finished tasks
     */
    List<Task> finishTasks(List<Task> tasks, IUser finisher = userService.loggedOrSystem, Map<String, String> params = [:]) {
        List<FinishTaskEventOutcome> outcomes = taskService.finishTasks(tasks, finisher, params)
        this.outcomes.addAll(outcomes)
        return outcomes.collect { it.task }
    }

    List<Task> findTasks(Closure<Predicate> predicate) {
        QTask qTask = new QTask("task")
        Page<Task> result = taskService.searchAll(predicate(qTask))
        return result.content
    }

    List<Task> findTasks(Closure<Predicate> predicate, Pageable pageable) {
        QTask qTask = new QTask("task")
        Page<Task> result = taskService.search(predicate(qTask), pageable)
        return result.content
    }

    /**
     * Finds tasks referenced by a field in its value.
     *
     * Use this overload when working on a case from current action context. For working with fields from out of the
     * current action context see other overloads of this action.
     *
     * <p>If the field value is {@code null}, this method returns an empty list.</p>
     * <p>If the value cannot be converted to task IDs, this method returns an empty list.</p>
     *
     * @param taskRef field whose value contains task IDs
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TASK_REF},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#ENUMERATION_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#STRING_COLLECTION},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TEXT},
     * @return list of matching tasks, or an empty list when the field value is {@code null}
     * @see ActionDelegate#findTasks(DataField)
     * @see ActionDelegate#findTasks(List<String>)
     */
    List<Task> findTasks(Field taskRef) {
        if(taskRef.value == null) {
            log.debug("[findTasks(Field)]: Value of field with id [${taskRef.importId}] is null, returning empty list.")
            return []
        }
        try {
            return this.findTasks([taskRef.value].flatten() as List<String>)
        } catch (ClassCastException e) {
            log.error("Method cannot be used with field with id [${taskRef.importId}].", e)
            return []
        }
    }
    
    /**
     * Finds tasks referenced by a dataField in its value.
     *
     * Use this overload when working on a case not from the current action context. For working with fields from out of the
     * current action context see other overloads of this action.
     *
     * <p>If the field value is {@code null}, this method returns an empty list.</p>
     * <p>If the value cannot be converted to task IDs, this method returns an empty list.</p>
     *
     * @param taskRef field whose value contains task IDs
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TASK_REF},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#ENUMERATION_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#STRING_COLLECTION},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TEXT},
     * @return list of matching tasks, or an empty list when the field value is {@code null}
     * @see ActionDelegate#findTasks(Field)
     * @see ActionDelegate#findTasks(List<String>)
     */
    List<Task> findTasks(DataField taskRef) {
        if(taskRef.value == null) {
            log.debug("[findTasks(DataField)]: Value of field is null, returning empty list.")
            return []
        }
        try {
            return this.findTasks([taskRef.value].flatten() as List<String>)
        } catch (ClassCastException e) {
            log.error("Method cannot be used with field.", e)
            return []
        }
    }

    /**
     * Finds tasks by their MongoDB IDs.
     *
     * @param mongoIds task identifiers
     * @return list of matching tasks, or an empty list when the input is {@code null} or {@code empty}
     * @see ActionDelegate#findTasks(Field)
     * @see ActionDelegate#findTasks(DataField)
     */
    List<Task> findTasks(List<String> mongoIds) {
        if(mongoIds == null || mongoIds.empty) {
            log.debug("[findTasks(List<String>)]: Null value detected, returning empty list.")
            return []
        }
        return taskService.findAllById(mongoIds)
    }

    Task findTask(Closure<Predicate> predicate) {
        QTask qTask = new QTask("task")
        return taskService.searchOne(predicate(qTask))
    }

    Task findTask(String mongoId) {
        return taskService.findOne(mongoId)
    }

    /**
     * Finds the first task referenced by a field in its value.
     *
     * Use this overload when working on a case from the current action context. For working with fields from out of the
     * current action context see other overloads of this action.
     *
     * <p>If the field value is {@code null}, this method returns {@code null}.</p>
     * <p>If the field contains no value or the value cannot be converted to a task ID, this method returns
     * {@code null}.</p>
     *
     * @param taskRef field whose value contains a task ID
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TASK_REF},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#ENUMERATION_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#STRING_COLLECTION},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TEXT},
     * @return referenced task, or {@code null} when the field value is invalid
     * @see ActionDelegate#findTask(DataField)
     * @see ActionDelegate#findTask(String)
     * @see ActionDelegate#findTask(Closure)
     */
    Task findTask(Field taskRef) {
        if(taskRef.value == null) {
            log.debug("[findTask(Field)]: Value of field with id [${taskRef.importId}] is null, returning null")
            return null
        }
        try {
            List<String> castValue = [taskRef.value].flatten() as List<String>
            if(castValue.size() == 0) {
                log.debug("[findTask(Field)]: Value of field with id [${taskRef.importId}] does not contain at least one element, returning null.")
                return null
            }
            return this.findTask(castValue[0])
        } catch (ClassCastException e) {
            log.error("Method cannot be used with field with id [${taskRef.importId}].", e)
            return null
        }
    }

    /**
     * Finds the first task referenced by a dataField in its value.
     *
     * Use this overload when working on a case not from the current action context. For working with fields from out of the
     * current action context see other overloads of this action.
     *
     * <p>If the field value is {@code null}, this method returns {@code null}.</p>
     * <p>If the field contains no value or the value cannot be converted to a task ID, this method returns
     * {@code null}.</p>
     *
     * @param taskRef field whose value contains a task ID
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TASK_REF},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#MULTICHOICE_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#ENUMERATION_MAP},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#STRING_COLLECTION},
     * {@link com.netgrif.application.engine.petrinet.domain.dataset.FieldType#TEXT},
     * @return referenced task, or {@code null} when the field value is invalid
     * @see ActionDelegate#findTask(Field)
     * @see ActionDelegate#findTask(String)
     * @see ActionDelegate#findTask(Closure)
     */
    Task findTask(DataField taskRef) {
        if(taskRef.value == null) {
            log.debug("[findTask(DataField)]: Value of field is null, returning null")
            return null
        }
        try {
            List<String> castValue = [taskRef.value].flatten() as List<String>
            if(castValue.size() == 0) {
                log.debug("[findTask(DataField)]: Value of field does not contain at least one element, returning null.")
                return null
            }
            return this.findTask(castValue[0])
        } catch (ClassCastException e) {
            log.error("Method cannot be used with field.", e)
            return null
        }
    }

    /**
     * Finds a Petri net by its MongoDB ID.
     *
     * @param mongoId Petri net identifier
     * @return matching Petri net, or {@code null} when the input is {@code null}
     */
    PetriNet findPetriNet(String mongoId) {
        if(mongoId == null){
            log.debug("[findPetriNet(String)]: Null value detected, returning null.")
            return null
        }
        return petriNetService.getPetriNet(mongoId)
    }

    /**
     * Finds a Petri net by its {@link ObjectId}.
     *
     * @param objectId Petri net object identifier
     * @return matching Petri net, or {@code null} when the input is {@code null}
     */
    PetriNet findPetriNet(ObjectId objectId) {
        if(objectId == null){
            log.debug("[findPetriNet(ObjectId)]: Null value detected, returning null.")
            return null
        }
        return petriNetService.get(objectId)
    }

    /**
     * Finds Petri nets by their MongoDB IDs.
     *
     * @param mongoIds list of Petri net identifiers
     * @return matching Petri nets, or an empty list when the input is {@code null} or {@code empty}
     */
    List<PetriNet> findPetriNets(List<String> mongoIds) {
        if(mongoIds == null || mongoIds.empty){
            log.debug("[findPetriNets(List<String>)]: Null value detected, returning empty list.")
            return []
        }
        return petriNetService.findAllById(mongoIds)
    }

    /**
     * Finds Petri nets by their {@link ObjectId} values.
     *
     * @param objectIds list of Petri net object identifiers
     * @return matching Petri nets, or an empty list when the input is {@code null} or {@code empty}
     */
    List<PetriNet> findPetriNetsByObjectIds(List<ObjectId> objectIds) {
        if(objectIds == null || objectIds.empty){
            log.debug("[findPetriNetsByObjectIds(List<ObjectId>)]: Null value detected, returning empty list.")
            return []
        }
        return petriNetService.get(objectIds as Collection<ObjectId>)
    }

    /**
     * Finds a Petri net by its identifier and optional version.
     *
     * If the version is not provided, the newest available version is returned.
     *
     * @param identifier Petri net identifier
     * @param version requested version, or {@code null} for the newest version
     * @return matching Petri net, or {@code null} when the identifier is {@code null}
     */
    PetriNet findPetriNetByIdentifier(String identifier, Version version = null) {
        if(identifier == null) {
            log.debug("[findPetriNetByIdentifier(String, Version)]: Null identifier value detected, returning null.")
            return null
        }
        return version == null ? petriNetService.getNewestVersionByIdentifier(identifier) : petriNetService.getPetriNet(identifier, version)
    }

    /**
     * Converts cases to a map of option keys and translated option values.
     *
     * @param casesToTransform cases to convert
     * @param valueTransformation transformation used to derive the option label from a case, case title is used if not specified otherwise
     * @param keyTransformation transformation used to derive the option key from a case, case stringId is used if not specified otherwise
     * @return map of option keys and translated values
     */
    Map<String, I18nString> casesToOptions(List<Case> casesToTransform, Closure<String> valueTransformation = { return it.title }, Closure<String> keyTransformation = { return it.stringId }) {
        return casesToTransform.collectEntries {
            [(keyTransformation(it)): new I18nString(valueTransformation(it))]
        }
    }

    String getTaskId(String transitionId, Case aCase = useCase) {
        List<TaskReference> refs = taskService.findAllByCase(aCase.stringId, null)
        return refs.find { it.transitionId == transitionId }.stringId
    }

    /**
     * Returns task identifiers for tasks belonging to the provided transitions in the given case.
     *
     * @param transitionIds transition identifiers
     * @param aCase case whose tasks should be inspected, defaults to the current case
     * @return list of matching task identifiers
     */
    List<String> getTaskIds(List<String> transitionIds, Case aCase = useCase) {
        List<TaskReference> refs = taskService.findAllByCase(aCase.stringId, null)
        return refs.findAll { transitionIds.contains(it.transitionId) }.collect { it.stringId}
    }

    IUser assignRole(String roleMongoId, IUser user = userService.loggedUser) {
        IUser actualUser = userService.addRole(user, roleMongoId)
        return actualUser
    }

    IUser assignRole(String roleId, String netId, IUser user = userService.loggedUser) {
        List<PetriNet> nets = petriNetService.getByIdentifier(netId)
        nets.forEach({ net -> user = assignRole(roleId, net, user) })
        return user
    }

    IUser assignRole(String roleId, PetriNet net, IUser user = userService.loggedUser) {
        IUser actualUser = userService.addRole(user, net.roles.values().find { role -> role.importId == roleId }.stringId)
        return actualUser
    }

    IUser assignRole(String roleId, String netId, Version version, IUser user = userService.loggedUser) {
        PetriNet net = petriNetService.getPetriNet(netId, version)
        return assignRole(roleId, net, user)
    }

    IUser removeRole(String roleMongoId, IUser user = userService.loggedUser) {
        IUser actualUser = userService.removeRole(user, roleMongoId)
        return actualUser
    }

    IUser removeRole(String roleId, String netId, IUser user = userService.loggedUser) {
        List<PetriNet> nets = petriNetService.getByIdentifier(netId)
        nets.forEach({ net -> user = removeRole(roleId, net, user) })
        return user
    }

    IUser removeRole(String roleId, PetriNet net, IUser user = userService.loggedUser) {
        IUser actualUser = userService.removeRole(user, net.roles.values().find { role -> role.importId == roleId }.stringId)
        return actualUser
    }

    IUser removeRole(String roleId, String netId, Version version, IUser user = userService.loggedUser) {
        PetriNet net = petriNetService.getPetriNet(netId, version)
        return removeRole(roleId, net, user)
    }

    SetDataEventOutcome setData(Task task, Map dataSet, Map<String, String> params = [:]) {
        return setData(task.stringId, dataSet, params)
    }

    SetDataEventOutcome setData(String taskId, Map dataSet, Map<String, String> params = [:]) {
        return addSetDataOutcomeToOutcomes(dataService.setData(taskId, ImportHelper.populateDataset(dataSet), params))
    }

    SetDataEventOutcome setData(Transition transition, Map dataSet, Map<String, String> params = [:]) {
        return addSetDataOutcomeToOutcomes(setData(transition.importId, this.useCase, dataSet, params))
    }

    SetDataEventOutcome setData(String transitionId, Case useCase, Map dataSet, Map<String, String> params = [:]) {
        Task task = taskService.findOne(useCase.tasks.find { it.transition == transitionId }.task)
        return addSetDataOutcomeToOutcomes(dataService.setData(task.stringId, ImportHelper.populateDataset(dataSet), params))
    }

    @Deprecated
    SetDataEventOutcome setDataWithPropagation(String transitionId, Case caze, Map dataSet) {
        Task task = taskService.findOne(caze.tasks.find { it.transition == transitionId }.task)
        return setDataWithPropagation(task, dataSet)
    }

    @Deprecated
    SetDataEventOutcome setDataWithPropagation(Task task, Map dataSet) {
        return setDataWithPropagation(task.stringId, dataSet)
    }

    @Deprecated
    SetDataEventOutcome setDataWithPropagation(String taskId, Map dataSet) {
        Task task = taskService.findOne(taskId)
        return setData(task, dataSet)
    }

    private SetDataEventOutcome addSetDataOutcomeToOutcomes(SetDataEventOutcome outcome) {
        this.outcomes.add(outcome)
        return outcome
    }

    Map<String, ChangedField> makeDataSetIntoChangedFields(Map<String, Map<String, String>> map, Case caze, Task task) {
        return map.collect { fieldAttributes ->
            ChangedField changedField = new ChangedField(fieldAttributes.key)
            changedField.wasChangedOn(task)
            fieldAttributes.value.each { attribute ->
                changedField.addAttribute(attribute.key, attribute.value)
            }
            return changedField
        }.collectEntries {
            return [(it.id): (it)]
        }
    }

    Map<String, Field> getData(Task task, Map<String, String> params = [:]) {
        def useCase = workflowService.findOne(task.caseId)
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase, params)))
    }

    Map<String, Field> getData(String taskId, Map<String, String> params = [:]) {
        Task task = taskService.findById(taskId)
        def useCase = workflowService.findOne(task.caseId)
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase, params)))
    }

    Map<String, Field> getData(Transition transition, Map<String, String> params = [:]) {
        return getData(transition.stringId, this.useCase, params)
    }

    Map<String, Field> getData(String transitionId, Case useCase, Map<String, String> params = [:]) {
        def predicate = QTask.task.caseId.eq(useCase.stringId) & QTask.task.transitionId.eq(transitionId)
        def task = taskService.searchOne(predicate)
        if (!task)
            return new HashMap<String, Field>()
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase, params)))
    }

    private List<Field> addGetDataOutcomeToOutcomesAndReturnData(GetDataEventOutcome outcome) {
        this.outcomes.add(outcome)
        return outcome.getData()
    }

    protected Map<String, Field> mapData(List<Field> data) {
        return data.collectEntries {
            [(it.importId): it]
        }
    }

    IUser loggedUser() {
        return userService.loggedUser
    }

    void saveFileToField(Case targetCase, String targetTransitionId, String targetFieldId, String filename, String storagePath = null) {
        FileFieldValue fieldValue = new FileFieldValue()
        fieldValue.setName(filename)
        if (!storagePath) {
            storagePath = fieldValue.getPath(targetCase.stringId, targetFieldId)
        }
        fieldValue.setPath(storagePath)
        if (targetCase.stringId == useCase.stringId) {
            change targetCase.getField(targetFieldId) value { fieldValue }
        } else {
            String taskId = targetCase.getTasks().find(taskPair -> taskPair.transition == targetTransitionId).task
            def dataSet = [
                    targetFieldId: [
                            "value": filename + ":" + storagePath,
                            "type" : "file"
                    ]
            ]
            setData(taskId, dataSet)
        }
    }

    @NamedVariant
    void generatePdf(String sourceTransitionId, String targetFileFieldId,
                     Case sourceCase = useCase, Case targetCase = useCase, String targetTransitionId = null,
                     String template = null, List<String> excludedFields = [], Locale locale = null,
                     ZoneId dateZoneId = ZoneId.systemDefault(), Integer sideMargin = 75, Integer titleMargin = 0) {
        if (!sourceTransitionId || !targetFileFieldId)
            throw new IllegalArgumentException("Source transition or target file field is null")
        targetTransitionId = targetTransitionId ?: sourceTransitionId
        PdfResource pdfResource = ApplicationContextProvider.getBean(PdfResource.class) as PdfResource
        String filename = pdfResource.getOutputDefaultName()
        String storagePath
        if (pdfResource.getOutputFolder()) {
            storagePath = pdfResource.getOutputFolder() + File.separator + targetCase.stringId + "-" + targetFileFieldId + "-" + filename
        } else {
            storagePath = new FileFieldValue(filename, "").getPath(targetCase.stringId, targetFileFieldId)
        }

        pdfResource.setOutputResource(new ClassPathResource(storagePath))
        if (template) {
            pdfResource.setTemplateResource(new FileSystemResource(template))
        }
        if (locale) {
            pdfResource.setTextLocale(locale)
        }
        pdfResource.setDateZoneId(dateZoneId)
        pdfResource.setMarginTitle(titleMargin)
        pdfResource.setMarginLeft(sideMargin)
        pdfResource.setMarginRight(sideMargin)
        pdfResource.updateProperties()
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(sourceCase, sourceTransitionId, pdfResource, excludedFields)
        saveFileToField(targetCase, targetTransitionId, targetFileFieldId, filename, storagePath)
    }

    void generatePdf(Transition sourceTransition, FileField targetFileField, Case sourceCase = useCase, Case targetCase = useCase,
                     Transition targetTransition = null, String template = null, List<String> excludedFields = [], Locale locale = null,
                     ZoneId dateZoneId = ZoneId.systemDefault(), Integer sideMargin = 75, Integer titleMargin = 0) {
        if (!sourceTransition || !targetFileField)
            throw new IllegalArgumentException("Source transition or target file field is null")
        targetTransition = targetTransition ?: sourceTransition
        generatePdf(sourceTransition.stringId, targetFileField.importId, sourceCase, targetCase, targetTransition.stringId,
                template, excludedFields, locale, dateZoneId, sideMargin, titleMargin)
    }

    @NamedVariant
    void generatePDF(String sourceTransitionId, String targetFileFieldId,
                     Case sourceCase = useCase, Case targetCase = useCase, String targetTransitionId = null,
                     String template = null, List<String> excludedFields = [], Locale locale = null,
                     ZoneId dateZoneId = ZoneId.systemDefault(), Integer sideMargin = 75, Integer titleMargin = 20) {
        if (!sourceTransitionId || !targetFileFieldId)
            throw new IllegalArgumentException("Source transition or target file field is null")
        targetTransitionId = targetTransitionId ?: sourceTransitionId
        generatePdf(sourceTransitionId, targetFileFieldId, sourceCase, targetCase, targetTransitionId,
                template, excludedFields, locale, dateZoneId, sideMargin, titleMargin)
    }

    void generatePDF(Transition sourceTransition, FileField targetFileField, Case sourceCase = useCase, Case targetCase = useCase,
                     Transition targetTransition = null, String template = null, List<String> excludedFields = [], Locale locale = null,
                     ZoneId dateZoneId = ZoneId.systemDefault(), Integer sideMargin = 75, Integer titleMargin = 0) {
        if (!sourceTransition || !targetFileField)
            throw new IllegalArgumentException("Source transition or target file field is null")
        targetTransition = targetTransition ?: sourceTransition
        generatePdf(sourceTransition.stringId, targetFileField.importId, sourceCase, targetCase, targetTransition.stringId,
                template, excludedFields, locale, dateZoneId, sideMargin, titleMargin)
    }

    void generatePdf(String transitionId, FileField fileField, List<String> excludedFields = []) {
        generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileField, excludedFields: excludedFields)
    }

    void generatePdf(String transitionId, String fileFieldId, List<String> excludedFields, Case fromCase = useCase, Case saveToCase = useCase) {
        generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileFieldId, excludedFields: excludedFields, sourceCase: fromCase, targetCase: useCase)
    }

    void generatePdfWithTemplate(String transitionId, String fileFieldId, String template, Case fromCase = useCase, Case saveToCase = useCase) {
        generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileFieldId, template: template, sourceCase: fromCase, targetCase: saveToCase)
    }

    void generatePdfWithLocale(String transitionId, String fileFieldId, Locale locale, Case fromCase = useCase, Case saveToCase = useCase) {
        generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileFieldId, locale: locale, sourceCase: fromCase, targetCase: saveToCase)
    }

    void generatePdfWithZoneId(String transitionId, String fileFieldId, ZoneId dateZoneId = ZoneId.systemDefault(), Case fromCase = useCase, Case saveToCase = useCase) {
        generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileFieldId, dateZoneId: dateZoneId, sourceCase: fromCase, targetCase: saveToCase)
    }

    void sendEmail(List<String> to, String subject, String body) {
        MailDraft mailDraft = MailDraft.builder(mailFrom, to).subject(subject).body(body).build()
        sendMail(mailDraft)
    }

    void sendEmail(List<String> to, String subject, String body, Map<String, File> attachments) {
        MailDraft mailDraft = MailDraft.builder(mailFrom, to).subject(subject).body(body).attachments(attachments).build()
        sendMail(mailDraft)
    }

    void sendMail(MailDraft mailDraft) {
        mailService.sendMail(mailDraft)
    }

    def changeUserByEmail(String email) {
        [email  : { cl ->
            changeUserByEmail(email, "email", cl)
        },
         name   : { cl ->
             changeUserByEmail(email, "name", cl)
         },
         surname: { cl ->
             changeUserByEmail(email, "surname", cl)
         },
         tel    : { cl ->
             changeUserByEmail(email, "tel", cl)
         },
        ]
    }

    def changeUser(String id) {
        [email  : { cl ->
            changeUser(id, "email", cl)
        },
         name   : { cl ->
             changeUser(id, "name", cl)
         },
         surname: { cl ->
             changeUser(id, "surname", cl)
         },
         tel    : { cl ->
             changeUser(id, "tel", cl)
         },
        ]
    }

    def changeUser(IUser user) {
        [email  : { cl ->
            changeUser(user, "email", cl)
        },
         name   : { cl ->
             changeUser(user, "name", cl)
         },
         surname: { cl ->
             changeUser(user, "surname", cl)
         },
         tel    : { cl ->
             changeUser(user, "tel", cl)
         },
        ]
    }

    def changeUserByEmail(String email, String attribute, def cl) {
        IUser user = userService.findByEmail(email, false)
        changeUser(user, attribute, cl)
    }

    def changeUser(String id, String attribute, def cl) {
        IUser user = userService.findById(id, false)
        changeUser(user, attribute, cl)
    }

    def changeUser(IUser user, String attribute, def cl) {
        if (user == null) {
            log.error("Cannot find user.")
            return
        }

        if (user.hasProperty(attribute) == null) {
            log.error("User object does not have property [" + attribute + "]")
            return
        }

        user[attribute] = cl() as String
        userService.save(user)
    }

    MessageResource inviteUser(String email) {
        NewUserRequest newUserRequest = new NewUserRequest()
        newUserRequest.email = email
        newUserRequest.groups = new HashSet<>()
        newUserRequest.processRoles = new HashSet<>()
        return inviteUser(newUserRequest)
    }

    MessageResource inviteUser(NewUserRequest newUserRequest) {
        IUser user = registrationService.createNewUser(newUserRequest)
        if (user == null)
            return MessageResource.successMessage("Done")
        mailService.sendRegistrationEmail(user)

        mailAttemptService.mailAttempt(newUserRequest.email)
        return MessageResource.successMessage("Done")
    }

    void deleteUser(String email) {
        IUser user = userService.findByEmail(email, false)
        if (user == null)
            log.error("Cannot find user with email [" + email + "]")
        deleteUser(user)
    }

    void deleteUser(IUser user) {
        List<Task> tasks = taskService.findByUser(new FullPageRequest(), user).toList()
        if (tasks != null && tasks.size() > 0)
            taskService.cancelTasks(tasks, user)

        QCase qCase = new QCase("case")
        List<Case> cases = workflowService.searchAll(qCase.author.eq(user.transformToAuthor())).toList()
        if (cases != null)
            cases.forEach({ aCase -> aCase.setAuthor(Author.createAnonymizedAuthor()) })

        userService.deleteUser(user)
    }

    IUser findUserByEmail(String email) {
        IUser user = userService.findByEmail(email, false)
        if (user == null) {
            log.error("Cannot find user with email [" + email + "]")
            return null
        } else {
            return user
        }
    }

    IUser findUserById(String id) {
        IUser user = userService.findById(id, false)
        if (user == null) {
            log.error("Cannot find user with id [" + id + "]")
            return null
        } else {
            return user
        }
    }

    Validation validation(String rule, I18nString message) {
        return new Validation(rule, message)
    }

    DynamicValidation dynamicValidation(String rule, I18nString message) {
        return new DynamicValidation(rule, message)
    }

    File exportCasesToFile(Closure<Predicate> predicate, String pathName, ExportDataConfig config = null,
                           int pageSize = exportConfiguration.getMongoPageSize()) {
        File exportFile = new File(pathName)
        OutputStream out = exportCases(predicate, exportFile, config, pageSize)
        out.close()
        return exportFile
    }

    OutputStream exportCases(Closure<Predicate> predicate, File outFile, ExportDataConfig config = null,
                             int pageSize = exportConfiguration.getMongoPageSize()) {
        QCase qCase = new QCase("case")
        return exportService.fillCsvCaseData(predicate(qCase), outFile, config, pageSize)
    }

    File exportCasesToFile(List<CaseSearchRequest> requests, String pathName, ExportDataConfig config = null,
                           LoggedUser user = userService.loggedOrSystem.transformToLoggedUser(),
                           int pageSize = exportConfiguration.getMongoPageSize(),
                           Locale locale = LocaleContextHolder.getLocale(),
                           Boolean isIntersection = false) {
        File exportFile = new File(pathName)
        OutputStream out = exportCases(requests, exportFile, config, user, pageSize, locale, isIntersection)
        out.close()
        return exportFile
    }

    OutputStream exportCases(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config = null,
                             LoggedUser user = userService.loggedOrSystem.transformToLoggedUser(),
                             int pageSize = exportConfiguration.getMongoPageSize(),
                             Locale locale = LocaleContextHolder.getLocale(),
                             Boolean isIntersection = false) {
        return exportService.fillCsvCaseData(requests, outFile, config, user, pageSize, locale, isIntersection)
    }

    File exportTasksToFile(Closure<Predicate> predicate, String pathName, ExportDataConfig config = null) {
        File exportFile = new File(pathName)
        OutputStream out = exportTasks(predicate, exportFile, config)
        out.close()
        return exportFile
    }

    OutputStream exportTasks(Closure<Predicate> predicate, File outFile, ExportDataConfig config = null, int pageSize = exportConfiguration.getMongoPageSize()) {
        QTask qTask = new QTask("task")
        return exportService.fillCsvTaskData(predicate(qTask), outFile, config, pageSize)
    }

    File exportTasksToFile(List<ElasticTaskSearchRequest> requests, String pathName, ExportDataConfig config = null,
                           LoggedUser user = userService.loggedOrSystem.transformToLoggedUser(),
                           int pageSize = exportConfiguration.getMongoPageSize(),
                           Locale locale = LocaleContextHolder.getLocale(),
                           Boolean isIntersection = false) {
        File exportFile = new File(pathName)
        OutputStream out = exportTasks(requests, exportFile, config, user, pageSize, locale, isIntersection)
        out.close()
        return exportFile
    }

    OutputStream exportTasks(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config = null,
                             LoggedUser user = userService.loggedOrSystem.transformToLoggedUser(),
                             int pageSize = exportConfiguration.getMongoPageSize(),
                             Locale locale = LocaleContextHolder.getLocale(),
                             Boolean isIntersection = false) {
        return exportService.fillCsvTaskData(requests, outFile, config, user, pageSize, locale, isIntersection)
    }

    FileFieldInputStream getFileFieldStream(Case useCase, Task task, FileField field, boolean forPreview = false) {
        return this.dataService.getFile(useCase, task, field, forPreview)
    }

    def getUri(String uri) {
        return uriService.findByUri(uri)
    }

    def createUri(String uri, UriContentType type) {
        return uriService.getOrCreate(uri, type)
    }

    def moveUri(String uri, String dest) {
        return uriService.move(uri, dest)
    }

    /**
     * Action API case search function using Elasticsearch database
     * @param requests the CaseSearchRequest list
     * @param loggedUser the user who is searching for the requests
     * @param page the order of page to return. by default it returns the first page
     * @param pageable the page configuration that will contain the requests
     * @param locale the Locale to be used when searching for requests
     * @param isIntersection to decide null query handling
     * @return page of cases
     * */
    Page<Case> findCasesElastic(List<CaseSearchRequest> requests, LoggedUser loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
                                int page = 1, int pageSize = 25, Locale locale = Locale.default, boolean isIntersection = false) {
        return elasticCaseService.search(requests, loggedUser, PageRequest.of(page, pageSize), locale, isIntersection)
    }

    /**
     * Action API case search function using Elasticsearch database
     * @param request case search request
     * @param page the order of page to return
     * @param loggedUser the user who is searching for the requests
     * @param pageable the page configuration that will contain the requests
     * @param locale the Locale to be used when searching for requests
     * @param isIntersection to decide null query handling
     * @return page of cases
     * */
    Page<Case> findCasesElastic(Map<String, Object> request, LoggedUser loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
                                int page = 1, int pageSize = 25, Locale locale = Locale.default, boolean isIntersection = false) {
        List<CaseSearchRequest> requests = Collections.singletonList(new CaseSearchRequest(request))
        return findCasesElastic(requests, loggedUser, page, pageSize, locale, isIntersection)
    }

    /**
     * Action API task search function using Elasticsearch database
     * @param requests the @link{ElasticTaskSearchRequest} list
     * @param loggedUser the user who is searching for the requests
     * @param page the order of page to return. by default it returns the first page
     * @param pageable the page configuration that will contain the requests
     * @param locale the Locale to be used when searching for requests
     * @param isIntersection to decide null query handling
     * @return page of cases
     * */
    Page<Task> findTasksElastic(List<ElasticTaskSearchRequest> requests, LoggedUser loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
                         int page = 1, int pageSize = 25, Locale locale = Locale.default, boolean isIntersection = false) {
        return elasticTaskService.search(requests, loggedUser, PageRequest.of(page, pageSize), locale, isIntersection)
    }

    /**
     * Action API task search function using Elasticsearch database
     * @param request case search request
     * @param loggedUser the user who is searching for the requests
     * @param page the order of page to return. by default it returns the first page
     * @param pageable the page configuration that will contain the requests
     * @param locale the Locale to be used when searching for requests
     * @param isIntersection to decide null query handling
     * @return page of cases
     * */
    Page<Task> findTasksElastic(Map<String, Object> request, LoggedUser loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
                         int page = 1, int pageSize = 25, Locale locale = Locale.default, boolean isIntersection = false) {
        List<ElasticTaskSearchRequest> requests = Collections.singletonList(new ElasticTaskSearchRequest(request))
        return findTasksElastic(requests, loggedUser, page, pageSize, locale, isIntersection)
    }

    /**
     * Changes data of provided menu_item instance. These attributes can be changed:
     * <ul>
     * <li> <code>changeMenuItem item allowedRoles { ["role_1":"my_process_id"] }</code>
     * <li> <code>changeMenuItem item bannedRoles { ["role_1":"my_process_id"] }</code>
     * <li> <code>changeMenuItem item uri { "/my_node1/my_node2" }</code>
     * <li> <code>changeMenuItem item title { new I18nString("New title") }</code>
     * <li> <code>changeMenuItem item title { "New title" }</code>
     * <li> <code>changeMenuItem item menuIcon { "filter_alt" }</code>
     * <li> <code>changeMenuItem item tabIcon { "filter_none" }</code>
     * </ul>
     * @param item {@link Case} instance of menu_item.xml
     */
    def changeMenuItem(Case item) {
        [allowedRoles      : { cl ->
            updateMenuItemRoles(item, cl as Closure, MenuItemConstants.FIELD_ALLOWED_ROLES)
        },
         bannedRoles       : { cl ->
             updateMenuItemRoles(item, cl as Closure, MenuItemConstants.FIELD_BANNED_ROLES)
         },
         uri               : { cl ->
             def uri = cl() as String
             def aCase = useCase
             if (useCase == null || item.stringId != useCase.stringId) {
                 aCase = workflowService.findOne(item.stringId)
             }
             moveMenuItem(aCase, uri)
         },
         title             : { cl ->
             def value = cl()
             I18nString newName = (value instanceof I18nString) ? value : new I18nString(value as String)
             setData(MenuItemConstants.TRANS_SYNC_ID, item, [
                     (MenuItemConstants.FIELD_MENU_NAME): ["type": "i18n", "value": newName]
             ])
         },
         menuIcon          : { cl ->
             def value = cl()
             setData(MenuItemConstants.TRANS_SYNC_ID, item, [
                     (MenuItemConstants.FIELD_MENU_ICON): ["type": "text", "value": value]
             ])
         },
         tabIcon           : { cl ->
             def value = cl()
             setData(MenuItemConstants.TRANS_SYNC_ID, item, [
                     (MenuItemConstants.FIELD_TAB_ICON): ["type": "text", "value": value]
             ])
         },
         useCustomView     : { cl ->
             def value = cl()
             setData(MenuItemConstants.TRANS_SYNC_ID, item, [
                     (MenuItemConstants.FIELD_USE_CUSTOM_VIEW): ["type": "boolean", "value": value]
             ])
         },
         customViewSelector: { cl ->
             def value = cl()
             setData(MenuItemConstants.TRANS_SYNC_ID, item, [
                     (MenuItemConstants.FIELD_CUSTOM_VIEW_SELECTOR): ["type": "text", "value": value]
             ])
         }]

    }

    void updateMenuItemRoles(Case item, Closure cl, String roleFieldId) {
        item = workflowService.findOne(item.stringId)
        def roles = cl()
        def dataField = item.dataSet[roleFieldId]
        if (roles instanceof List<ProcessRole>) {
            dataField.options = collectRolesForPreferenceItem(roles)
        } else if (roles instanceof Map<String, String>) {
            dataField.options = collectRolesForPreferenceItem(roles)
        }
        workflowService.save(item)
    }

    /**
     * delete menu item (referenced filter instance will not be deleted)
     * @param item
     * @return
     */
    def deleteMenuItem(Case item) {
        async.run {
            workflowService.deleteCase(item.stringId)
        }
    }

    Case createMenuItem(MenuItemBody body) {
        return menuItemService.createMenuItem(body)
    }

    protected ViewBody createLegacyMenuItemViews(FilterBody filterBody, List<String> caseDefaultHeaders = null,
                                                 List<String> taskDefaultHeaders = null) {
        if (filterBody.getType() == "Case") {
            ViewBody caseView = new CaseViewBody()
            caseView.setFilterBody(filterBody)
            caseView.setDefaultHeaders(caseDefaultHeaders)
            caseView.setRequireTitleInCreation(true)

            ViewBody taskView = new TaskViewBody()
            taskView.setDefaultHeaders(taskDefaultHeaders)
            caseView.setChainedView(taskView)

            return caseView
        } else if (filterBody.getType() == "Task") {
            ViewBody taskView = new TaskViewBody()
            taskView.setFilterBody(filterBody)
            taskView.setDefaultHeaders(taskDefaultHeaders)
            return taskView
        }
        return null
    }

    /**
     * Changes location of menu item. If non-existing location is provided, the new location is created and then the
     * item is moved. Cyclic destination path is forbidden (f.e. from <code>"/my_node"</code> to
     * <code>"/my_node/my_node2"</code>
     *
     * @param item Instance of menu_item to be moved
     * @param destUri destination path where the item will be moved. F.e. <code>"/my_new_node"</code>
     * */
    void moveMenuItem(Case item, String destUri) {
        menuItemService.moveItem(item, destUri)
    }

    /**
     * Duplicates menu item. It creates new menu_item instance with the same dataSet as the provided
     * item instance. The only difference is in title, menu_item_identifier and associations. Configuration cases are
     * duplicated as well.
     *
     * @param originItem Menu item instance, which is duplicated
     * @param newTitle Title of menu item, that is displayed in menu and tab. Cannot be empty or null.
     * @param newIdentifier unique menu item identifier
     *
     * @return duplicated {@link Case} instance of menu_item
     *
     * @throws IllegalArgumentException if the input data are invalid or the menu item of the new identifier already
     * exists
     * */
    Case duplicateMenuItem(Case originItem, I18nString newTitle, String newIdentifier) {
        return menuItemService.duplicateItem(originItem, newTitle, newIdentifier)
    }

    /**
     * Finds menu item by unique identifier
     *
     * @param menuItemIdentifier unique menu item identifier
     *
     * @return found menu_item instance. Can be null
     */
    Case findMenuItem(String menuItemIdentifier) {
        return menuItemService.findMenuItem(menuItemIdentifier)
    }

    /**
     * @param node uri node
     *
     * @return folder menu item case by provided UriNode
     * */
    Case findFolderCase(UriNode node) {
        return menuItemService.findFolderCase(node)
    }

    /**
     * Checks the menu item existence.
     *
     * @param menuItemIdentifier unique menu item identifier
     *
     * @return true if the item exists
     * */
    boolean existsMenuItem(String menuItemIdentifier) {
        return menuItemService.existsMenuItem(menuItemIdentifier)
    }

    /**
     * find menu item by uri and name in default group
     * @param uri
     * @param name
     * @return
     */
    Case findMenuItem(String uri, String name) {
        return menuItemService.findMenuItem(uri, name)
    }

    /**
     * find menu item by uri, title and name of group
     * @param uri
     * @param name
     * @param groupName
     * @return
     */
    @Deprecated
    Case findMenuItem(String uri, String name, String groupName) {
        return findMenuItem(uri, name)
    }

    /**
     *
     * @param uri
     * @param name
     * @param orgGroup
     * @return
     */
    @Deprecated
    Case findMenuItemInGroup(String uri, String name, Case orgGroup) {
        return findMenuItem(uri, name)
    }

    Case createDashboardManagement(DashboardManagementBody body) {
        return dashboardManagementService.createDashboardManagement(body)
    }

    Case createDashboardItem(DashboardItemBody body) {
        return dashboardItemService.getOrCreate(body)
    }

    Case findDashboardManagement(String identifier) {
        return dashboardManagementService.findDashboardManagement(identifier)
    }

    Case findDashboardItem(String identifier) {
        return dashboardItemService.findById(identifier)
    }

    Case updateDashboardManagement(Case managementCase, DashboardManagementBody body) {
        return dashboardManagementService.updateDashboardManagement(managementCase, body)
    }

    Case updateDashboardItem(Case itemCase, DashboardItemBody body) {
        return dashboardItemService.update(itemCase, body)
    }

    /**
     * search elastic with string query for first occurrence
     * @param query string with search conditions
     * @return one case which match search condition or null
     */
    Case findCaseElastic(String query) {
        def result = findCasesElastic(query, PageRequest.of(0, 1))
        return result ? result[0] : null
    }

    /**
     * search elastic with string query for cases and default page size of 100 cases
     * @param query string with search conditions
     * @param pageSize optional parameter which decides number of returned elements
     * @return list of cases (default max 100) which match condition
     */
    List<Case> findCasesElastic(String query, int pageSize = 100) {
        this.findCasesElastic(query, PageRequest.of(0, pageSize))
    }

    /**
     * search elastic with string query for cases
     * @param query string with search conditions
     * @param pageable object which decides page size, page number and order of elements
     * @return list of cases (size and order depends on pageable object) which match condition
     */
    List<Case> findCasesElastic(String query, Pageable pageable) {
        CaseSearchRequest request = new CaseSearchRequest()
        request.query = query
        List<Case> result = elasticCaseService.search([request], userService.system.transformToLoggedUser(), pageable, LocaleContextHolder.locale, false).content
        return result
    }

    /**
     * find count of cases which match condition
     * @param query string with search conditions
     * @return number of cases which match condition
     */
    long countCasesElastic(String query) {
        CaseSearchRequest request = new CaseSearchRequest()
        request.query = query
        return elasticCaseService.count([request], userService.system.transformToLoggedUser(), LocaleContextHolder.locale, false)
    }

    @Deprecated
    private Case findMenuItemByUriNameProcessAndGroup(String uri, String name, Case orgGroup) {
        return findMenuItem(uri, name)
    }

    Map<String, I18nString> collectRolesForPreferenceItem(List<ProcessRole> roles) {
        return roles.collectEntries { role ->
            if (role.isGlobal()) {
                return [(role.importId + ":" + GLOBAL_ROLE), ("$role.name (🌍 Global role)" as String)]
            } else {
                PetriNet net = petriNetService.get(new ObjectId(role.netId))
                return [(role.importId + ":" + net.identifier), ("$role.name ($net.title)" as String)]
            }
        } as Map<String, I18nString>
    }

    Map<String, I18nString> collectRolesForPreferenceItem(Map<String, String> roles) {
        Map<String, PetriNet> temp = [:]
        return roles.collectEntries { entry ->
            if (entry.value == GLOBAL_ROLE) {
                Set<ProcessRole> findGlobalRole = processRoleService.findAllByImportId(ProcessRole.GLOBAL + entry.key)
                if (findGlobalRole == null || findGlobalRole.isEmpty()) {
                    return
                }
                ProcessRole role = findGlobalRole.find { it.isGlobal() }
                if (role == null) {
                    return
                }
                return [(role.importId + ":" + GLOBAL_ROLE), ("$role.name (🌍 Global role)" as String)]
            } else {
                if (!temp.containsKey(entry.value)) {
                    temp.put(entry.value, petriNetService.getNewestVersionByIdentifier(entry.value))
                }
                PetriNet net = temp[entry.value]
                ProcessRole role = net.roles.find { it.value.importId == entry.key }.value
                return [(role.importId + ":" + net.identifier), ("$role.name ($net.title)" as String)]
            }
        } as Map<String, I18nString>
    }

    I18nString i18n(String value, Map<String, String> translations) {
        return new I18nString(value, translations)
    }

    /**
     * Creates or updates menu item with given identifier.
     *
     * @param body data for menu item
     *
     * @return created or updated menu item instance
     * */
    Case createOrUpdateMenuItem(MenuItemBody body) {
        return menuItemService.createOrUpdateMenuItem(body)
    }

    /**
     * Creates menu item or ignores it if already exists
     *
     * @param body configuration class for menu item
     *
     * @return created or existing menu item instance
     * */
    Case createOrIgnoreMenuItem(MenuItemBody body) {
        return menuItemService.createOrIgnoreMenuItem(body)
    }

    /**
     * Updates existing menu item with provided values.
     *
     * @param item Menu item instance to be updated
     * @param body data to update in menu item instance
     *
     * @return updated menu item instance
     * */
    Case updateMenuItem(Case item, MenuItemBody body) {
        return menuItemService.updateMenuItem(item, body)
    }

    void removeChildItemFromParent(String folderId, Case childItem) {
        menuItemService.removeChildItemFromParent(folderId, childItem)
    }

    String makeUrl(String publicViewUrl = publicViewProperties.url, String identifier) {
        return "${publicViewUrl}/${Base64.getEncoder().encodeToString(identifier.bytes)}" as String
    }

    void updateMultichoiceWithCurrentNode(MultichoiceMapField field, UriNode node) {
        List<String> splitPathList = splitUriPath(node.uriPath)

        change field options { findOptionsBasedOnSelectedNode(node, splitPathList) }
        change field value { splitPathList }
    }

    List<String> splitUriPath(String uri) {
        String rootUri = uriService.getUriSeparator()
        String[] splitPath = uri.split(uriService.getUriSeparator())
        if (splitPath.length == 0 && uri == rootUri) {
            splitPath = [rootUri]
        } else if (splitPath.length == 0) {
            throw new IllegalArgumentException("Wrong uri value: \"${uri}\"")
        } else {
            splitPath[0] = rootUri
        }
        return splitPath as ArrayList
    }

    Map<String, I18nString> findOptionsBasedOnSelectedNode(UriNode node) {
        return findOptionsBasedOnSelectedNode(node, splitUriPath(node.uriPath))
    }

    Map<String, I18nString> findOptionsBasedOnSelectedNode(UriNode node, List<String> splitPathList) {
        Map<String, I18nString> options = new HashMap<>()

        options.putAll(splitPathList.collectEntries { [(it): new I18nString(it)] })

        Set<String> childrenIds = node.getChildrenId()
        if (!childrenIds.isEmpty()) {
            for (String id : childrenIds) {
                UriNode childNode = uriService.findById(id)
                options.put(childNode.name, new I18nString(childNode.name))
            }
        }

        return options
    }

    String getCorrectedUri(String uncheckedUri) {
        String rootUri = uriService.getUriSeparator()
        if (uncheckedUri == "") {
            return rootUri
        }

        UriNode node = uriService.findByUri(uncheckedUri)

        while (node == null) {
            int lastIdx = uncheckedUri.lastIndexOf(uriService.getUriSeparator())
            if (lastIdx == -1) {
                return rootUri
            }
            uncheckedUri = uncheckedUri.substring(0, uncheckedUri.lastIndexOf(uriService.getUriSeparator()))
            if (uncheckedUri == "") {
                return rootUri
            }
            node = uriService.findByUri(uncheckedUri)
        }

        return node.uriPath
    }

    Field<?> getFieldOfTask(String taskId, String fieldId) {
        Task task = taskService.findOne(taskId)
        Case taskCase = workflowService.findOne(task.caseId)
        return taskCase.getPetriNet().getDataSet().get(fieldId)
    }

    PetriNet importPetriNet(String xmlText) {
        InputStream xmlStream = new ByteArrayInputStream(xmlText.bytes)
        def outcome = petriNetService.importPetriNet(xmlStream, loggedUser().transformToLoggedUser())
        return outcome.getNet()
    }

    /**
     * Searches for a single {@link Case} matching the given query.
     * <p>
     * The query must start with the resource keyword {@code case} (singular).
     * </p>
     * Example:
     * <pre>
     *     searchCase("case: processIdentifier eq 'query_test' and data.number_0.value == 3")
     *     searchCase("case: id eq '5f9b1c2d3e4f5a6b7c8d9e0f'")
     * </pre>
     *
     * @param query query language string starting with {@code case:}
     * @return matching {@link Case} or {@code null} if none is found
     */
    Case searchCase(String query) {
        return caseSearchService.searchOne(query)
    }

    /**
     * Searches for all {@link Case} instances matching the given query and returns a paged result.
     * <p>
     * The query must start with the resource keyword {@code cases} (plural) and may contain
     * paging and sorting clauses.
     * </p>
     * Example:
     * <pre>
     *     pagedSearchCases("cases: processIdentifier eq 'query_test' page 1 size 5 sort by title desc")
     *     pagedSearchCases("cases: author eq 'user@mail.com' and creationDate gt 2020-03-03")
     * </pre>
     *
     * @param query query language string starting with {@code cases:}
     * @return {@link Page} of matching cases
     */
    Page<Case> pagedSearchCases(String query) {
        return caseSearchService.searchAll(query)
    }

    /**
     * Searches for all {@link Case} instances matching the given query and returns them as a list.
     * <p>
     * The query must start with the resource keyword {@code cases} (plural). This is a convenience
     * method returning only the content of {@link #pagedSearchCases(String)}.
     * </p>
     * Example:
     * <pre>
     *     searchCases("cases: processIdentifier eq 'query_test' and data.boolean_0.value == true")
     *     searchCases("cases: title contains 'Test' sort by creationDate desc")
     * </pre>
     *
     * @param query query language string starting with {@code cases:}
     * @return list of matching cases
     */
    List<Case> searchCases(String query) {
        return pagedSearchCases(query).content
    }

    /**
     * Counts the number of {@link Case} instances matching the given query.
     * <p>
     * The query must start with the resource keyword {@code cases} (plural).
     * </p>
     * Example:
     * <pre>
     *     countCases("cases: processIdentifier eq 'query_test'")
     *     countCases("cases: data.boolean_0.value == true and data.text_0.value != '4'")
     * </pre>
     *
     * @param query query language string starting with {@code cases:}
     * @return number of matching cases
     */
    long countCases(String query) {
        return caseSearchService.count(query)
    }

    /**
     * Checks whether at least one {@link Case} matching the given query exists.
     * <p>
     * The query must start with the resource keyword {@code cases} (plural).
     * </p>
     * Example:
     * <pre>
     *     existsCase("cases: processIdentifier eq 'query_test'")
     *     existsCase("cases: id in ('5f9b1c2d3e4f5a6b7c8d9e0f', '5f9b1c2d3e4f5a6b7c8d9e10')")
     * </pre>
     *
     * @param query query language string starting with {@code cases:}
     * @return {@code true} if a matching case exists, {@code false} otherwise
     */
    boolean existsCase(String query) {
        return caseSearchService.exists(query)
    }

    /**
     * Searches for a single {@link Task} matching the given query.
     * <p>
     * The query must start with the resource keyword {@code task} (singular).
     * </p>
     * Example:
     * <pre>
     *     searchTask("task: transitionId eq 't1' and caseId eq '5f9b1c2d3e4f5a6b7c8d9e0f'")
     *     searchTask("task: id eq '5f9b1c2d3e4f5a6b7c8d9e0f'")
     * </pre>
     *
     * @param query query language string starting with {@code task:}
     * @return matching {@link Task} or {@code null} if none is found
     */
    Task searchTask(String query) {
        return taskSearchService.searchOne(query)
    }

    /**
     * Searches for all {@link Task} instances matching the given query and returns a paged result.
     * <p>
     * The query must start with the resource keyword {@code tasks} (plural) and may contain
     * paging and sorting clauses.
     * </p>
     * Example:
     * <pre>
     *     pagedSearchTasks("tasks: title eq 'test' page 0 size 10 sort by lastFinish desc")
     *     pagedSearchTasks("tasks: userId eq 'user1' and state eq enabled")
     * </pre>
     *
     * @param query query language string starting with {@code tasks:}
     * @return {@link Page} of matching tasks
     */
    Page<Task> pagedSearchTasks(String query) {
        return taskSearchService.searchAll(query)
    }

    /**
     * Searches for all {@link Task} instances matching the given query and returns them as a list.
     * <p>
     * The query must start with the resource keyword {@code tasks} (plural). This is a convenience
     * method returning only the content of {@link #pagedSearchTasks(String)}.
     * </p>
     * Example:
     * <pre>
     *     searchTasks("tasks: processId eq 'my_process' and userId in ('user1', 'user2')")
     *     searchTasks("tasks: title contains 'Approve' sort by title asc")
     * </pre>
     *
     * @param query query language string starting with {@code tasks:}
     * @return list of matching tasks
     */
    List<Task> searchTasks(String query) {
        return pagedSearchTasks(query).content
    }

    /**
     * Counts the number of {@link Task} instances matching the given query.
     * <p>
     * The query must start with the resource keyword {@code tasks} (plural).
     * </p>
     * Example:
     * <pre>
     *     countTasks("tasks: caseId eq '5f9b1c2d3e4f5a6b7c8d9e0f'")
     *     countTasks("tasks: transitionId eq 't1' and userId eq 'user1'")
     * </pre>
     *
     * @param query query language string starting with {@code tasks:}
     * @return number of matching tasks
     */
    long countTasks(String query) {
        return taskSearchService.count(query)
    }

    /**
     * Checks whether at least one {@link Task} matching the given query exists.
     * <p>
     * The query must start with the resource keyword {@code tasks} (plural).
     * </p>
     * Example:
     * <pre>
     *     existsTask("tasks: caseId eq '5f9b1c2d3e4f5a6b7c8d9e0f'")
     *     existsTask("tasks: transitionId eq 't1' and userId not eq 'user1'")
     * </pre>
     *
     * @param query query language string starting with {@code tasks:}
     * @return {@code true} if a matching task exists, {@code false} otherwise
     */
    boolean existsTask(String query) {
        return taskSearchService.exists(query)
    }

    /**
     * Searches for a single {@link PetriNet} (process) matching the given query.
     * <p>
     * The query must start with the resource keyword {@code process} (singular).
     * </p>
     * Example:
     * <pre>
     *     searchProcess("process: identifier == 'query_test'")
     *     searchProcess("process: identifier eq 'my_process' and version eq 1.0.0")
     * </pre>
     *
     * @param query query language string starting with {@code process:}
     * @return matching {@link PetriNet} or {@code null} if none is found
     */
    PetriNet searchProcess(String query) {
        return processSearchService.searchOne(query)
    }

    /**
     * Searches for all {@link PetriNet} (process) instances matching the given query and returns a paged result.
     * <p>
     * The query must start with the resource keyword {@code processes} (plural) and may contain
     * paging and sorting clauses.
     * </p>
     * Example:
     * <pre>
     *     pagedSearchProcesses("processes: identifier eq 'my_process' page 0 size 10 sort by version desc")
     *     pagedSearchProcesses("processes: version in (1.0.0 : 2.0.0)")
     * </pre>
     *
     * @param query query language string starting with {@code processes:}
     * @return {@link Page} of matching processes
     */
    Page<PetriNet> pagedSearchProcesses(String query) {
        return processSearchService.searchAll(query)
    }

    /**
     * Searches for all {@link PetriNet} (process) instances matching the given query and returns them as a list.
     * <p>
     * The query must start with the resource keyword {@code processes} (plural). This is a convenience
     * method returning only the content of {@link #pagedSearchProcesses(String)}.
     * </p>
     * Example:
     * <pre>
     *     searchProcesses("processes: title contains 'Test' sort by identifier asc")
     *     searchProcesses("processes: identifier in ('process_a', 'process_b')")
     * </pre>
     *
     * @param query query language string starting with {@code processes:}
     * @return list of matching processes
     */
    List<PetriNet> searchProcesses(String query) {
        return pagedSearchProcesses(query).content
    }

    /**
     * Counts the number of {@link PetriNet} (process) instances matching the given query.
     * <p>
     * The query must start with the resource keyword {@code processes} (plural).
     * </p>
     * Example:
     * <pre>
     *     countProcesses("processes: identifier eq 'my_process'")
     *     countProcesses("processes: version gte 1.0.0")
     * </pre>
     *
     * @param query query language string starting with {@code processes:}
     * @return number of matching processes
     */
    long countProcesses(String query) {
        return processSearchService.count(query)
    }

    /**
     * Checks whether at least one {@link PetriNet} (process) matching the given query exists.
     * <p>
     * The query must start with the resource keyword {@code processes} (plural).
     * </p>
     * Example:
     * <pre>
     *     existsProcess("processes: identifier eq 'my_process'")
     *     existsProcess("processes: version eq 1.0.0")
     * </pre>
     *
     * @param query query language string starting with {@code processes:}
     * @return {@code true} if a matching process exists, {@code false} otherwise
     */
    boolean existsProcess(String query) {
        return processSearchService.exists(query)
    }

    /**
     * Searches for a single {@link IUser} matching the given query.
     * <p>
     * The query must start with the resource keyword {@code user} (singular).
     * </p>
     * Example:
     * <pre>
     *     searchUser("user: email eq 'user@mail.com'")
     *     searchUser("user: name eq 'John' and surname eq 'Doe'")
     * </pre>
     *
     * @param query query language string starting with {@code user:}
     * @return matching {@link IUser} or {@code null} if none is found
     */
    IUser searchUser(String query) {
        return userSearchService.searchOne(query)
    }

    /**
     * Searches for all {@link IUser} instances matching the given query and returns a paged result.
     * <p>
     * The query must start with the resource keyword {@code users} (plural) and may contain
     * paging and sorting clauses.
     * </p>
     * Example:
     * <pre>
     *     pagedSearchUsers("users: name eq 'John' page 0 size 25 sort by surname asc")
     *     pagedSearchUsers("users: email contains '@company.com'")
     * </pre>
     *
     * @param query query language string starting with {@code users:}
     * @return {@link Page} of matching users
     */
    Page<IUser> pagedSearchUsers(String query) {
        return userSearchService.searchAll(query)
    }

    /**
     * Searches for all {@link IUser} instances matching the given query and returns them as a list.
     * <p>
     * The query must start with the resource keyword {@code users} (plural). This is a convenience
     * method returning only the content of {@link #pagedSearchUsers(String)}.
     * </p>
     * Example:
     * <pre>
     *     searchUsers("users: surname eq 'Doe' sort by name asc")
     *     searchUsers("users: email in ('a@mail.com', 'b@mail.com')")
     * </pre>
     *
     * @param query query language string starting with {@code users:}
     * @return list of matching users
     */
    List<IUser> searchUsers(String query) {
        return pagedSearchUsers(query).content
    }

    /**
     * Counts the number of {@link IUser} instances matching the given query.
     * <p>
     * The query must start with the resource keyword {@code users} (plural).
     * </p>
     * Example:
     * <pre>
     *     countUsers("users: email contains '@company.com'")
     *     countUsers("users: name eq 'John'")
     * </pre>
     *
     * @param query query language string starting with {@code users:}
     * @return number of matching users
     */
    long countUsers(String query) {
        return userSearchService.count(query)
    }

    /**
     * Checks whether at least one {@link IUser} matching the given query exists.
     * <p>
     * The query must start with the resource keyword {@code users} (plural).
     * </p>
     * Example:
     * <pre>
     *     existsUser("users: email eq 'user@mail.com'")
     *     existsUser("users: name eq 'John' and surname eq 'Doe'")
     * </pre>
     *
     * @param query query language string starting with {@code users:}
     * @return {@code true} if a matching user exists, {@code false} otherwise
     */
    boolean existsUser(String query) {
        return userSearchService.exists(query)
    }

    /**
     * Generic search that resolves the resource type from the query itself and executes the search.
     * <p>
     * The query must start with one of the resource keywords: {@code process}/{@code processes},
     * {@code case}/{@code cases}, {@code task}/{@code tasks} or {@code user}/{@code users}.
     * When the singular form is used, a single matching instance is returned. When the plural form
     * is used, the content (a {@link List}) of the resulting {@link Page} is returned.
     * </p>
     * Example:
     * <pre>
     *     search("case: processIdentifier eq 'query_test' and data.number_0.value == 3")
     *     search("cases: processIdentifier eq 'query_test' page 1 size 5 sort by title desc")
     *     search("process: identifier == 'query_test'")
     * </pre>
     *
     * @param query query language string starting with a resource keyword
     * @return a single resource instance (singular form), a {@link List} of instances (plural form),
     *         or {@code null} if nothing matches
     */
    Object search(String query) {
        Object result = searchService.search(query)
        if (result instanceof Page<?>) {
            return result.content
        }
        return result
    }

    /**
     * Generic count that resolves the resource type from the query itself and counts matching instances.
     * <p>
     * The query must start with one of the resource keywords: {@code processes}, {@code cases},
     * {@code tasks} or {@code users} (plural form).
     * </p>
     * Example:
     * <pre>
     *     count("cases: processIdentifier eq 'query_test' and data.boolean_0.value == true")
     *     count("users: email contains '@company.com'")
     * </pre>
     *
     * @param query query language string starting with a resource keyword
     * @return number of matching instances
     */
    long count(String query) {
        return searchService.count(query)
    }

    /**
     * Generic existence check that resolves the resource type from the query itself.
     * <p>
     * The query must start with one of the resource keywords: {@code processes}, {@code cases},
     * {@code tasks} or {@code users} (plural form).
     * </p>
     * Example:
     * <pre>
     *     exists("cases: processIdentifier eq 'query_test'")
     *     exists("tasks: transitionId eq 't1' and userId eq 'user1'")
     * </pre>
     *
     * @param query query language string starting with a resource keyword
     * @return {@code true} if a matching instance exists, {@code false} otherwise
     */
    boolean exists(String query) {
        return searchService.exists(query)
    }
}