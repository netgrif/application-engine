//file:noinspection ChangeToOperator
package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.AsyncRunner

import com.netgrif.application.engine.authentication.domain.IUser
import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.service.UserDetailsServiceImpl
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService
import com.netgrif.application.engine.authentication.service.interfaces.IUserService
import com.netgrif.application.engine.authentication.web.requestbodies.NewIdentityRequest
import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.configuration.PublicViewProperties
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.application.engine.export.configuration.ExportConfiguration
import com.netgrif.application.engine.export.domain.ExportDataConfig
import com.netgrif.application.engine.export.service.interfaces.IExportService
import com.netgrif.application.engine.history.service.IHistoryService
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService
import com.netgrif.application.engine.importer.service.FieldFactory
import com.netgrif.application.engine.mail.domain.MailDraft
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService
import com.netgrif.application.engine.mail.interfaces.IMailService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.*
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.rules.domain.RuleRepository
import com.netgrif.application.engine.startup.DefaultFiltersRunner
import com.netgrif.application.engine.startup.FilterRunner
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.validations.interfaces.IValidationService
import com.netgrif.application.engine.workflow.domain.*
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome
import com.netgrif.application.engine.workflow.domain.menu.MenuItemBody
import com.netgrif.application.engine.workflow.domain.menu.MenuItemConstants
import com.netgrif.application.engine.workflow.service.FileFieldInputStream
import com.netgrif.application.engine.workflow.service.TaskService
import com.netgrif.application.engine.workflow.service.interfaces.*
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference
import com.querydsl.core.types.Predicate
import groovy.transform.NamedVariant
import groovy.util.logging.Slf4j
import org.bson.types.ObjectId
import org.quartz.Scheduler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

import java.text.Normalizer
import java.util.stream.Collectors

/**
 * ActionDelegate class contains Actions API methods.
 */
@Slf4j
@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class ActionDelegate /*TODO: release/8.0.0: implements ActionAPI*/ {

    private static final String FILTER_FIELD_I18N_FILTER_NAME = "i18n_filter_name"

    public static final String PREFERENCE_ITEM_FIELD_NEW_FILTER_ID = "new_filter_id"
    public static final String PREFERENCE_ITEM_FIELD_REMOVE_OPTION = "remove_option"
    public static final String PREFERENCE_ITEM_FIELD_FILTER_CASE = "filter_case"
    public static final String PREFERENCE_ITEM_FIELD_PARENT_ID = "parentId"
    public static final String PREFERENCE_ITEM_FIELD_DEFAULT_HEADERS = "default_headers"
    public static final String PREFERENCE_ITEM_FIELD_IDENTIFIER = "menu_item_identifier"
    public static final String PREFERENCE_ITEM_FIELD_APPEND_MENU_ITEM = "append_menu_item_stringId"
    public static final String PREFERENCE_ITEM_FIELD_ALLOWED_ROLES = "allowed_roles"
    public static final String PREFERENCE_ITEM_FIELD_BANNED_ROLES = "banned_roles"
    public static final String ORG_GROUP_FIELD_FILTER_TASKS = "filter_tasks"

    static final String TRANSITIONS = "transitions"

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
    IMailService mailService

    @Autowired
    INextGroupService nextGroupService

    @Autowired
    IRegistrationService registrationService

    @Autowired
    IMailAttemptService mailAttemptService

    @Autowired
    UserDetailsServiceImpl userDetailsService

    @Autowired
    IInitValueExpressionEvaluator initValueExpressionEvaluator

    @Autowired
    RuleRepository ruleRepository

    @Autowired
    Scheduler scheduler

    @Autowired
    IUserFilterSearchService filterSearchService

    @Autowired
    IConfigurableMenuService configurableMenuService

    @Autowired
    IMenuImportExportService menuImportExportService

    @Autowired
    IFilterImportExportService filterImportExportService

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
    IValidationService validationService

    @Autowired
    IRoleService roleService

    FrontendActionOutcome Frontend

    /**
     * Reference of case and task in which current action is taking place.
     */
    Case useCase
    Optional<Task> task
    Map<String, String> params
    def map = [:]
    Action action
    Field<?> fieldChanges
    ActionRunner actionsRunner
    List<EventOutcome> outcomes

    // TODO: release/8.0.0 - <action trigger="set" type="value">
    // TODO: release/8.0.0 - pretazit findCase, findTask - querydsl alebo caserequest, int page,int size
    // TODO: release/8.0.0 - setdata with user
    // TODO: release/8.0.0 - deprecate enum/multichoice with chooices, keep only maps with options

    void init(Action action, Case useCase, Optional<Task> task, Field<?> fieldChanges, ActionRunner actionsRunner, Map<String, String> params = [:]) {
        this.action = action
        this.useCase = useCase
        this.task = task
        this.fieldChanges = fieldChanges
        this.params = params
        // TODO: release/8.0.0 init net resources as delegate properties
        this.actionsRunner = actionsRunner
        this.outcomes = new ArrayList<>()
        this.Frontend = new FrontendActionOutcome(this.useCase, this.task, this.outcomes)
    }

    void initFieldsMap(Map<String, String> fieldIds, Case useCase) {
        fieldIds.each { name, id ->
            set(name, useCase.getDataSet().get(id))
        }
    }

    def initTransitionsMap(Map<String, String> transitionIds) {
        transitionIds.each { name, id ->
            set(name, useCase.process.transitions[id])
        }
    }

    // TODO: release/8.0.0 check merge, Case useCase
    void copyBehavior(Field field, Transition transition) {
        Field<?> caseField = useCase.dataSet.get(field.stringId)
        if (caseField.behaviors.get(transition.stringId) == null) {
            caseField.behaviors.put(transition.stringId, transition.dataSet.get(field.stringId).behavior)
        }
    }

    private DataFieldBehavior getOrCreateBehavior(String fieldId, String transitionId) {
        DataFieldBehavior dataFieldBehavior = useCase.dataSet.get(fieldId).behaviors.get(transitionId)
        if (dataFieldBehavior == null) {
            dataFieldBehavior = new DataFieldBehavior()
            useCase.dataSet.get(fieldId).behaviors.put(transitionId, dataFieldBehavior)
        }
        return dataFieldBehavior
    }

    // TODO: docasna metoda na priradenie behavior fieldu (make na 8.0.0 nefunguje)
    DataFieldBehaviors createBehavior(String fieldId, FieldBehavior fieldBehavior, String transitionId = task.get().transitionId, Case caze = useCase) {
        Field<?> caseField = useCase.dataSet.get(fieldId)
        if (caseField.behaviors == null) {
            caseField.behaviors = new DataFieldBehaviors()
            caze.dataSet.get(fieldId).behaviors.put(transitionId, new DataFieldBehavior())
        }
        if (caseField.behaviors.get(transitionId) == null && caze.getPetriNet().getTransition(transitionId) != null && caze.getPetriNet().getTransition(transitionId).dataSet.get(fieldId) != null) {
            caseField.behaviors.put(transitionId, caze.getPetriNet().getTransition(transitionId).dataSet.get(fieldId).behavior)
        }
        caseField.behaviors.behaviors.get(transitionId).behavior = fieldBehavior
        return caseField.behaviors
    }

    def visible = { Field field, Transition trans ->
        copyBehavior(field, trans)
        getOrCreateBehavior(field.stringId, trans.stringId).behavior = FieldBehavior.VISIBLE
    }

    def editable = { Field field, Transition trans ->
        copyBehavior(field, trans)
        getOrCreateBehavior(field.stringId, trans.stringId).behavior = FieldBehavior.EDITABLE
    }

    def required = { Field field, Transition trans ->
        copyBehavior(field, trans)
        getOrCreateBehavior(field.stringId, trans.stringId).required = true
    }

    def optional = { Field field, Transition trans ->
        copyBehavior(field, trans)
        getOrCreateBehavior(field.stringId, trans.stringId).required = false
    }

    def hidden = { Field field, Transition trans ->
        copyBehavior(field, trans)
        getOrCreateBehavior(field.stringId, trans.stringId).behavior = FieldBehavior.HIDDEN
    }

    def forbidden = { Field field, Transition trans ->
        copyBehavior(field, trans)
        getOrCreateBehavior(field.stringId, trans.stringId).behavior = FieldBehavior.FORBIDDEN
    }

    def initial = { Field field, Transition trans ->
        copyBehavior(field, trans)
        DataFieldBehavior fieldBehavior = getOrCreateBehavior(field.stringId, trans.stringId)
        DataFieldBehavior initialBehavior = useCase?.process?.dataSet?.get(field.importId)?.behaviors?.get(trans?.importId)
        if (initialBehavior == null) {
            initialBehavior = new DataFieldBehavior()
        }
        fieldBehavior.behavior = initialBehavior.behavior
        fieldBehavior.required = initialBehavior.required
        fieldBehavior.immediate = initialBehavior.immediate
    }

    def initValueOfField = { Field field, Map<String, String> params = [:] ->
        if (!field.hasDefault()) {
            return null
        } else if (field.isDynamicDefaultValue()) {
            return initValueExpressionEvaluator.evaluateValue(useCase, field, params)
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
    def make(Field<?> field, Closure<DataFieldBehavior> behavior) {
        [on: { Object transitionObject ->
            [when: { Closure condition ->
                if (!condition()) {
                    return
                }
                makeFieldBehaveOnTransition(field, behavior, transitionObject)
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
    def make(List<Field<?>> fields, Closure behavior) {
        [on: { Object transitionObject ->
            [when: { Closure condition ->
                if (!condition()) {
                    return
                }
                fields.forEach { field ->
                    makeFieldBehaveOnTransition(field, behavior, transitionObject)
                }
            }]
        }]
    }

    void makeFieldBehaveOnTransition(Field<?> field, Closure behavior, Object transitionObject) {
// TODO: release/8.0.0 check merge, example 4 & 5 added
//        if (transitionObject instanceof Task)
        if (transitionObject instanceof Transition) {
            changeBehaviourAndSave(field, behavior, transitionObject)
        } else if (transitionObject instanceof Collection<Transition>) {
            transitionObject.each { trans ->
                changeBehaviourAndSave(field, behavior, trans)
            }
        } else if (transitionObject instanceof Closure && transitionObject == transitions) {
            useCase.process.transitions.each { transitionEntry ->
                changeBehaviourAndSave(field, behavior, transitionEntry.value)
            }
        } else {
            throw new IllegalArgumentException("Invalid call of make method. Method call should contain specific transition (transitions) or keyword \'transitions\'.")
        }
    }

    SetDataEventOutcome setData(Field<?> field, Map changes, IUser user = userService.loggedOrSystem) {
        SetDataEventOutcome outcome = dataService.setData(useCase, new DataSet([
                (field.stringId): field.class.newInstance(changes)
        ] as Map<String, Field<?>>), user)
        this.outcomes.add(outcome)
        updateCase()
        return outcome
    }

    void changeBehaviourAndSave(Field<?> field, Closure<DataFieldBehavior> behavior, Transition trans) {
        if (!trans.dataSet.containsKey(field.stringId)) {
            return
        }
        behavior(field, trans)
        setData(field, [behaviors: useCase.dataSet.get(field.importId).behaviors])
    }

    // TODO: release/8.0.0 check if needed after merge
    protected void saveFieldBehaviorWithTask(Field<?> field, Task task, Closure behavior, def behaviorClosureResult) {
        Case aCase = workflowService.findOne(task.caseId)
        Transition transition = aCase.getProcess().getTransition(task.getTransitionId())
        behaviorClosureResult = behavior(field, transition, aCase)
        saveFieldBehavior(field, transition, (behavior == initial) ? behaviorClosureResult as Set : null, aCase, Optional.of(task))
    }

    protected SetDataEventOutcome createSetDataEventOutcome(Case useCase = this.useCase, Optional<Task> task = this.task) {
        return new SetDataEventOutcome(useCase, task.orElse(null))
    }

    // TODO: release/8.0.0 target case, merge check
//    def saveChangedValidation(Field field) {
//        // TODO: release/8.0.0 setData?
//        Field<?> caseField = useCase.dataSet.get(field.stringId)
//        caseField.validations = field.validations
//        List<Validation> compiled = field.validations.collect { it.clone() }
//        compiled.findAll { it instanceof DynamicValidation }.collect { (DynamicValidation) it }.each {
//            it.compiledRule = dataValidationExpressionEvaluator.compile(targetCase, it.expression)
//        }
//        SetDataEventOutcome outcome = createSetDataEventOutcome()
//        outcome.addChangedField(field.stringId, caseField)
//        this.outcomes.add(outcome)
//    }

    def execute(String taskId) {
        [with : { DataSet dataSet ->
            executeTasks(dataSet, taskId, { it.setStringId.isNotNull() })
        },
         where: { Closure<Predicate> closure ->
             [with: { DataSet dataSet ->
                 executeTasks(dataSet, taskId, closure)
             }]
         }]
    }

    def execute(Task task) {
        [with : { DataSet dataSet ->
            executeTasks(dataSet, task.stringId, { it.setStringId.isNotNull() })
        },
         where: { Closure<Predicate> closure ->
             [with: { DataSet dataSet ->
                 executeTasks(dataSet, task.stringId, closure)
             }]
         }]
    }

    void executeTasks(DataSet dataSet, String taskId, Closure<Predicate> predicateClosure) {
        List<String> caseIds = searchCases(predicateClosure)
        QTask qTask = new QTask("task")
        Page<Task> tasksPage = taskService.searchAll(qTask.transitionId.eq(taskId).and(qTask.caseId.in(caseIds)))
        tasksPage?.content?.each { task ->
            addTaskOutcomes(task, dataSet)
        }
    }

    void executeTask(String transitionId, DataSet dataSet) {
        QTask qTask = new QTask("task")
        Task task = taskService.searchOne(qTask.transitionId.eq(transitionId).and(qTask.caseId.eq(useCase.stringId)))
        addTaskOutcomes(task, dataSet)
    }

    private addTaskOutcomes(Task task, DataSet dataSet) {
        this.outcomes.add(taskService.assignTask(task.stringId))
        this.outcomes.add(dataService.setData(task.stringId, dataSet, userService.loggedOrSystem))
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
        Field field = targetCase.getProcess().getDataSet().get(fieldId)
        // TODO: release/8.0.0 missing
        change(field, targetCase, Optional.of(targetTask))
    }

    // TODO: release/8.0.0 merge targetCase
    def change(Field field) {
        [about      : { cl -> // TODO: deprecated
            changeFieldValue(field, cl)
        },
         value      : { cl ->
             changeFieldValue(field, cl)
         },
         choices    : { cl ->
             if (!(field instanceof MultichoiceField || field instanceof EnumerationField)) {
                 return
             }

             def values = cl()
             if (values == null || (values instanceof Closure && values() == UNCHANGED_VALUE)) {
                 return
             }


             if (!(values instanceof Collection)) {
                 values = [values]
             }
             field = (ChoiceField) field
             if (values.every { it instanceof I18nString }) {
                 field.setChoices(values as Set<I18nString>)
             } else {
                 field.setChoicesFromStrings(values as Set<String>)
             }
             setData(field, [choices: field.choices])
         },
         allowedNets: { cl ->
             if (!(field instanceof CaseField)) {// TODO make this work with FilterField as well
                 return
             }
             def allowedNets = cl()
             // TODO: release/8.0.0 unchaged
             if (allowedNets instanceof Closure && allowedNets() == UNCHANGED_VALUE) {
                 return
             }
             field = (CaseField) field
             if (allowedNets == null) {
                 field.setAllowedNets(new ArrayList<String>())
             } else if (allowedNets instanceof List) {
                 field.setAllowedNets(allowedNets)
             } else {
                 return
             }
             setData(field, [allowedNets: field.allowedNets])
         },
         options    : { cl ->
             if (!(field instanceof MultichoiceMapField || field instanceof EnumerationMapField
                     || field instanceof MultichoiceField || field instanceof EnumerationField))
                 return

             def options = cl()
             // TODO: release/8.0.0 unchaged
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
                 setData(field, [options: field.options])
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
                 setData(field, [choices: field.choices])
             }

         },
        ]
    }

    // TODO: release/8.0.0 targetCase
    void changeFieldValue(Field field, def cl) {
        def value = cl()
        if (value instanceof Closure) {
            if (value == initValueOfField) {
                value = initValueOfField(field)
// TODO: release/8.0.0 unchaged
            } else if (value() == UNCHANGED_VALUE) {
                return
            }
        }
        if (value == null && useCase.dataSet.get(field.stringId).value != null) {
            // TODO: release/8.0.0 should be in data service
            if (field instanceof FileListField && task.isPresent()) {
                field.value.value.namesPaths.forEach(namePath -> {
                    dataService.deleteFileByName(task.get().stringId, field.stringId, namePath.name)
                })
            }
            if (field instanceof FileField && task.isPresent()) {
                dataService.deleteFile(task.get().stringId, field.stringId)
            }
            setData(field, [rawValue: null])
        }
        if (value != null) {
            // TODO: release/8.0.0 should be in data service
            if (field instanceof CaseField) {
                if (value.every {it == null}) {
                    return;
                }
                value = ((List) value).stream().map({ entry -> entry instanceof Case ? entry.getStringId() : entry }).collect(Collectors.toList())
                dataService.validateCaseRefValue((List<String>) value, ((CaseField) field).getAllowedNets())
            }
            if (field instanceof NumberField) {
                value = value as Double
            }
            if (field instanceof UserListField && (value instanceof String[] || value instanceof List)) {
                LinkedHashSet<UserFieldValue> users = new LinkedHashSet<>()
                value.each { id -> users.add(new UserFieldValue(userService.findById(id as String))) }
                value = new UserListFieldValue(users)
            }
            if (value instanceof GString) {
                value = value.toString()
            }
            setData(field, [rawValue: value])
        }
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

    Case findCase(String id) {
        return workflowService.findOne(id)
    }

    Case findCase(Closure<Predicate> predicate) {
        QCase qCase = new QCase("case")
        return workflowService.searchOne(predicate(qCase))
    }

    Case createCase(String identifier, String title = null, String color = "", IUser author = userService.loggedOrSystem, Locale locale = LocaleContextHolder.getLocale(), Map<String, String> params = [:]) {
        return workflowService.createCaseByIdentifier(identifier, title, color, author.transformToLoggedUser(), locale, params).getCase()
    }

    Case createCase(Process net, String title = net.defaultCaseName.getTranslation(locale), String color = "", IUser author = userService.loggedOrSystem, Locale locale = LocaleContextHolder.getLocale(), Map<String, String> params = [:]) {
        CreateCaseEventOutcome outcome = workflowService.createCase(net.stringId, title, color, author.transformToLoggedUser(), params)
        this.outcomes.add(outcome)
        return outcome.getCase()
    }

    Task assignTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        String taskId = getTaskId(transitionId, aCase)
        AssignTaskEventOutcome outcome = taskService.assignTask(user.transformToLoggedUser(), taskId, params)
        this.outcomes.add(outcome)
        updateCase()
        return outcome.getTask()
    }

    Task assignTask(Task task, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return addTaskOutcomeAndReturnTask(taskService.assignTask(task, user, params))
    }

    void assignTasks(List<Task> tasks, IUser assignee = userService.loggedOrSystem, Map<String, String> params = [:]) {
        this.outcomes.addAll(taskService.assignTasks(tasks, assignee, params))
    }

    Task cancelTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        String taskId = getTaskId(transitionId, aCase)
        return addTaskOutcomeAndReturnTask(taskService.cancelTask(user.transformToLoggedUser(), taskId, params))
    }

    Task cancelTask(Task task, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return addTaskOutcomeAndReturnTask(taskService.cancelTask(task, user, params))
    }

    void cancelTasks(List<Task> tasks, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        this.outcomes.addAll(taskService.cancelTasks(tasks, user, params))
    }

    private Task addTaskOutcomeAndReturnTask(TaskEventOutcome outcome) {
        this.outcomes.add(outcome)
        updateCase()
        return outcome.getTask()
    }

    void finishTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        String taskId = getTaskId(transitionId, aCase)
        addTaskOutcomeAndReturnTask(taskService.finishTask(user.transformToLoggedUser(), taskId, params))
    }

    void finishTask(Task task, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        addTaskOutcomeAndReturnTask(taskService.finishTask(task, user, params))
    }

    void finishTasks(List<Task> tasks, IUser finisher = userService.loggedOrSystem, Map<String, String> params = [:]) {
        this.outcomes.addAll(taskService.finishTasks(tasks, finisher, params))
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

    Task findTask(Closure<Predicate> predicate) {
        QTask qTask = new QTask("task")
        return taskService.searchOne(predicate(qTask))
    }

    Task findTask(String mongoId) {
        return taskService.searchOne(QTask.task.id.eq(new ObjectId(mongoId)))
    }

    String getTaskId(String transitionId, Case aCase = useCase) {
        List<TaskReference> refs = taskService.findAllByCase(aCase.stringId, null)
        refs.find { it.transitionId == transitionId }.stringId
    }

    /**
     * todo javadoc
     * */
    Role assignRole(String roleId, String userId = userService.loggedUser.stringId , Map<String, String> params = this.params) {
        List<Role> roleAsList = assignRoles([roleId] as Set, userId, params)
        return roleAsList.isEmpty() ? null : roleAsList[0]
    }

    /**
     * todo javadoc
     * */
    List<Role> assignRoles(Set<String> roleIds, String userId = userService.loggedUser.stringId, Map<String, String> params = this.params) {
        return roleService.assignRolesToUser(userId, roleIds, params)
    }

    /**
     * todo javadoc
     * */
    Role removeRole(String roleId, String userId = userService.loggedUser.stringId, Map<String, String> params = this.params) {
        List<Role> roleAsList = removeRoles([roleId] as Set, userId, params)
        return roleAsList.isEmpty() ? null : roleAsList[0]
    }

    /**
     * todo javadoc
     * */
    List<Role> removeRoles(Set<String> roleIds, String userId = userService.loggedUser.stringId, Map<String, String> params = this.params) {
        return roleService.removeRolesFromUser(userId, roleIds, params)
    }

    // TODO: release/8.0.0 merge check, params x dataset
    SetDataEventOutcome setData(DataSet dataSet, IUser user = userService.loggedOrSystem) {
        return addSetDataOutcomeToOutcomes(dataService.setData(useCase, dataSet, user))
    }

    SetDataEventOutcome setData(Task task, DataSet dataSet, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return setData(task.stringId, dataSet, user, params)
    }

    SetDataEventOutcome setData(String taskId, DataSet dataSet, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return addSetDataOutcomeToOutcomes(dataService.setData(taskId, dataSet, user, params))
    }

    SetDataEventOutcome setData(Transition transition, DataSet dataSet, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return addSetDataOutcomeToOutcomes(setData(transition.importId, this.useCase, dataSet, user, params))
    }

    SetDataEventOutcome setData(String transitionId, Case useCase, DataSet dataSet, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        def predicate = QTask.task.caseId.eq(useCase.stringId) & QTask.task.transitionId.eq(transitionId)
        def task = taskService.searchOne(predicate)
        return addSetDataOutcomeToOutcomes(dataService.setData(task.stringId, dataSet, user, params))
    }

    @Deprecated
    SetDataEventOutcome setDataWithPropagation(String transitionId, Case caze, DataSet dataSet) {
        Task task = taskService.findOne(caze.getTaskStringId(transitionId))
        return setDataWithPropagation(task, dataSet)
    }

    @Deprecated
    SetDataEventOutcome setDataWithPropagation(Task task, DataSet dataSet) {
        return setDataWithPropagation(task.stringId, dataSet)
    }

    @Deprecated
    SetDataEventOutcome setDataWithPropagation(String taskId, DataSet dataSet) {
        Task task = taskService.findOne(taskId)
        return setData(task, dataSet)
    }

    private SetDataEventOutcome addSetDataOutcomeToOutcomes(SetDataEventOutcome outcome) {
        this.outcomes.add(outcome)
        updateCase()
        return outcome
    }

    Map<String, Field> getData(Task task, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        def useCase = workflowService.findOne(task.caseId)
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase, user, params)))
    }

    Map<String, Field> getData(String taskId, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        Task task = taskService.findById(taskId)
        def useCase = workflowService.findOne(task.caseId)
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase, user, params)))
    }

    Map<String, Field> getData(Transition transition, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        return getData(transition.stringId, this.useCase, user, params)
    }

    Map<String, Field> getData(String transitionId, Case useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        def predicate = QTask.task.caseId.eq(useCase.stringId) & QTask.task.transitionId.eq(transitionId)
        def task = taskService.searchOne(predicate)
        if (!task) {
            return new HashMap<String, Field>()
        }
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase, user, params)))
    }

    private List<DataRef> addGetDataOutcomeToOutcomesAndReturnData(GetDataEventOutcome outcome) {
        this.outcomes.add(outcome)
        return outcome.getData()
    }

    // TODO: release/8.0.0 should return dataRef?
    protected Map<String, Field> mapData(List<DataRef> data) {
        return data.collectEntries {
            [(it.fieldId): it.field]
        }
    }

    IUser loggedUser() {
        return userService.loggedUser
    }

    void saveFileToField(Case targetCase, String targetTransitionId, String targetFieldId, String filename, String storagePath = null) {
        FileFieldValue fieldValue = new FileFieldValue()
        fieldValue.setName(filename)
        if (!storagePath) {
            storagePath = FileStorageConfiguration.getPath(targetCase.stringId, targetFieldId, filename)
        }
        fieldValue.setPath(storagePath)
        if (targetCase.stringId == useCase.stringId) {
            change targetCase.dataSet.get(targetFieldId) value { fieldValue }
        } else {
            String taskId = targetCase.getTaskStringId(targetTransitionId)
            DataSet dataSet = new DataSet([
                    (targetFieldId): new FileField(rawValue: fieldValue)
            ] as Map<String, Field<?>>)
            setData(taskId, dataSet)
        }
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
        IUser user = userService.findByEmail(email)
        changeUser(user, attribute, cl)
    }

    def changeUser(String id, String attribute, def cl) {
        IUser user = userService.findById(id)
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
        NewIdentityRequest newUserRequest = new NewIdentityRequest()
        newUserRequest.email = email
        newUserRequest.groups = new HashSet<>()
        newUserRequest.roles = new HashSet<>()
        return inviteUser(newUserRequest)
    }

    MessageResource inviteUser(NewIdentityRequest newUserRequest) {
        IUser user = registrationService.createNewIdentity(newUserRequest)
        if (user == null)
            return MessageResource.successMessage("Done")
        mailService.sendRegistrationEmail(user)

        mailAttemptService.mailAttempt(newUserRequest.email)
        return MessageResource.successMessage("Done")
    }

    void deleteUser(String email) {
        IUser user = userService.findByEmail(email)
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
        IUser user = userService.findByEmail(email)
        if (user == null) {
            log.error("Cannot find user with email [" + email + "]")
            return null
        } else {
            return user
        }
    }

    IUser findUserById(String id) {
        IUser user = userService.findById(id)
        if (user == null) {
            log.error("Cannot find user with id [" + id + "]")
            return null
        } else {
            return user
        }
    }

    Validation validation(String name, Arguments clientArguments, Arguments serverArguments, I18nString message) {
        return new Validation(name, clientArguments, serverArguments, message)
    }

    // TODO: release/8.0.0 remove?
//    DynamicValidation dynamicValidation(String rule, I18nString message) {
//        return new DynamicValidation(rule, message)
//    }

    List<Case> findFilters(String userInput) {
        return filterSearchService.autocompleteFindFilters(userInput)
    }

    List<Case> findAllFilters() {
        return filterSearchService.autocompleteFindFilters("")
    }

    FileFieldValue exportFilters(Collection<String> filtersToExport) {
        if (filtersToExport.isEmpty()) {
            return null
        }
        return filterImportExportService.exportFiltersToFile(filtersToExport)
    }

    List<String> importFilters() {
        return filterImportExportService.importFilters()
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
                           Identity user = userService.loggedOrSystem.transformToLoggedUser(),
                           int pageSize = exportConfiguration.getMongoPageSize(),
                           Locale locale = LocaleContextHolder.getLocale(),
                           Boolean isIntersection = false) {
        File exportFile = new File(pathName)
        OutputStream out = exportCases(requests, exportFile, config, user, pageSize, locale, isIntersection)
        out.close()
        return exportFile
    }

    OutputStream exportCases(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config = null,
                             Identity user = userService.loggedOrSystem.transformToLoggedUser(),
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
                           Identity user = userService.loggedOrSystem.transformToLoggedUser(),
                           int pageSize = exportConfiguration.getMongoPageSize(),
                           Locale locale = LocaleContextHolder.getLocale(),
                           Boolean isIntersection = false) {
        File exportFile = new File(pathName)
        OutputStream out = exportTasks(requests, exportFile, config, user, pageSize, locale, isIntersection)
        out.close()
        return exportFile
    }

    OutputStream exportTasks(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config = null,
                             Identity user = userService.loggedOrSystem.transformToLoggedUser(),
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
    Page<Case> findCasesElastic(List<CaseSearchRequest> requests, Identity loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
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
    Page<Case> findCasesElastic(Map<String, Object> request, Identity loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
                                int page = 1, int pageSize = 25, Locale locale = Locale.default, boolean isIntersection = false) {
        List<CaseSearchRequest> requests = Collections.singletonList(new CaseSearchRequest(request))
        return findCasesElastic(requests, loggedUser, page, pageSize, locale, isIntersection)
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
    Page<Task> findTasks(List<ElasticTaskSearchRequest> requests, Identity loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
                         int page = 1, int pageSize = 25, Locale locale = Locale.default, boolean isIntersection = false) {
        return elasticTaskService.search(requests, loggedUser, PageRequest.of(page, pageSize), locale, isIntersection)
    }

    /**
     * Action API case search function using Elasticsearch database
     * @param request case search request
     * @param loggedUser the user who is searching for the requests
     * @param page the order of page to return. by default it returns the first page
     * @param pageable the page configuration that will contain the requests
     * @param locale the Locale to be used when searching for requests
     * @param isIntersection to decide null query handling
     * @return page of cases
     * */
    Page<Task> findTasks(Map<String, Object> request, Identity loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
                         int page = 1, int pageSize = 25, Locale locale = Locale.default, boolean isIntersection = false) {
        List<ElasticTaskSearchRequest> requests = Collections.singletonList(new ElasticTaskSearchRequest(request))
        return findTasks(requests, loggedUser, page, pageSize, locale, isIntersection)
    }

    List<Case> findDefaultFilters() {
        if (!createDefaultFilters) {
            return []
        }
        return findCases({ it.processIdentifier.eq(FilterRunner.FILTER_PETRI_NET_IDENTIFIER).and(it.author.setStringId.eq(userService.system.stringId)) })
    }

    /**
     * Creates filter instance of type {@value DefaultFiltersRunner#FILTER_TYPE_CASE}
     *
     * @param title filter case title
     * @param query elastic query for the view
     * @param icon filter case icon
     * @param allowedNets List of process identifiers
     * @param visibility Possible values: {@value DefaultFiltersRunner#FILTER_VISIBILITY_PRIVATE} or {@value DefaultFiltersRunner#FILTER_VISIBILITY_PUBLIC}
     * @param filterMetadata metadata for filter. If no value is provided, then default value is used: {@link #defaultFilterMetadata(String)}
     *
     * @return created {@link Case} instance of filter
     */
    @NamedVariant
    Case createCaseFilter(def title, String query, List<String> allowedNets,
                          String icon = "", String visibility = DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE, def filterMetadata = null) {
        return createFilter(title, query, DefaultFiltersRunner.FILTER_TYPE_CASE, allowedNets, icon, visibility, filterMetadata)
    }

    /**
     * Creates filter instance of type {@value DefaultFiltersRunner#FILTER_TYPE_TASK}
     *
     * @param title filter case title
     * @param query elastic query for the view
     * @param icon filter case icon
     * @param allowedNets List of process identifiers
     * @param visibility Possible values: {@value DefaultFiltersRunner#FILTER_VISIBILITY_PRIVATE} or {@value DefaultFiltersRunner#FILTER_VISIBILITY_PUBLIC}
     * @param filterMetadata metadata for filter. If no value is provided, then default value is used: {@link #defaultFilterMetadata(String)}
     *
     * @return created {@link Case} instance of filter
     */
    @NamedVariant
    Case createTaskFilter(def title, String query, List<String> allowedNets,
                          String icon = "", String visibility = DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE, def filterMetadata = null) {
        return createFilter(title, query, DefaultFiltersRunner.FILTER_TYPE_TASK, allowedNets, icon, visibility, filterMetadata)
    }

    /**
     * Creates filter instance.
     *
     * @param title filter case title
     * @param query elastic query for the view
     * @param type Filter type. Possible values: {@value DefaultFiltersRunner#FILTER_TYPE_CASE} or {@value DefaultFiltersRunner#FILTER_TYPE_TASK}
     * @param icon filter case icon
     * @param allowedNets List of process identifiers
     * @param visibility Possible values: {@value DefaultFiltersRunner#FILTER_VISIBILITY_PRIVATE} or {@value DefaultFiltersRunner#FILTER_VISIBILITY_PUBLIC}
     * @param filterMetadata metadata for filter. If no value is provided, then default value is used: {@link #defaultFilterMetadata(String)}
     *
     * @return created {@link Case} instance of filter
     */
    @NamedVariant
    Case createFilter(def title, String query, String type, List<String> allowedNets,
                      String icon, String visibility, def filterMetadata) {
        Case filterCase = createCase(FilterRunner.FILTER_PETRI_NET_IDENTIFIER, title as String)
        filterCase.setIcon(icon)
        filterCase.dataSet.get(DefaultFiltersRunner.FILTER_I18N_TITLE_FIELD_ID).rawValue = (title instanceof I18nString) ? title : new I18nString(title as String)
        filterCase = workflowService.save(filterCase)
        Task newFilterTask = findTask(filterCase.getTaskStringId(DefaultFiltersRunner.AUTO_CREATE_TRANSITION))
        assignTask(newFilterTask)

        // TODO: release/8.0.0 filterMetadata ?: defaultFilterMetadata(type)
        DataSet dataSet = new DataSet([
                (DefaultFiltersRunner.FILTER_TYPE_FIELD_ID)      : new EnumerationMapField(rawValue: type),
                (DefaultFiltersRunner.FILTER_VISIBILITY_FIELD_ID): new EnumerationMapField(rawValue: visibility),
                (DefaultFiltersRunner.FILTER_FIELD_ID)           : new FilterField(
                        rawValue: query,
                        allowedNets: allowedNets,
                        filterMetadata: filterMetadata ?: [
                                "searchCategories"       : [],
                                "predicateMetadata"      : [],
                                "filterType"             : type,
                                "defaultSearchCategories": true,
                                "inheritAllowedNets"     : false
                        ]
                )
        ] as Map<String, Field<?>>)
        setData(newFilterTask, dataSet)
        finishTask(newFilterTask)
        return workflowService.findOne(filterCase.stringId)
    }

    /**
     * Changes data of provided filter instance. These attributes can be changed:
     * <ul>
     * <li> <code>changeFilter filter query { "processIdentifier:"my_process_id" }</code>
     * <li> <code>changeFilter filter visibility { "private" }</code>
     * <li> <code>changeFilter filter allowedNets { ["my_process_id1","my_process_id2"] }</code>
     * <li> <pre>changeFilter filter filterMetadata { [
     "searchCategories"       : [],
     "predicateMetadata"      : [],
     "filterType"             : "Case",
     "defaultSearchCategories": true,
     "inheritAllowedNets"     : false
     ] }</pre>
     * <li> <code>changeFilter filter title { new I18nString("New title") }</code>
     * <li> <code>changeFilter filter title { "New title" }</code>
     * <li> <code>changeFilter filter icon { "filter_alt" }</code>
     * <li> <code>changeFilter filter uri { "/my_node1/my_node2" }</code>
     * </ul>
     * @param filter {@link Case} instance of filter
     */
    // TODO: release/8.0.0: missing test on changeFilter action?
    def changeFilter(Case filter) {
        [query         : { cl ->
            updateFilter(filter, new DataSet([
                    (DefaultFiltersRunner.FILTER_FIELD_ID): new EnumerationMapField(rawValue: cl() as String)
            ]))
        },
         visibility    : { cl ->
             updateFilter(filter, new DataSet([
                     (DefaultFiltersRunner.FILTER_VISIBILITY_FIELD_ID): new EnumerationMapField(rawValue: cl() as String)
             ]))
         },
         allowedNets   : { cl ->
             String currentQuery = workflowService.findOne(filter.stringId).dataSet.get(DefaultFiltersRunner.FILTER_FIELD_ID).rawValue
             updateFilter(filter, new DataSet([
                     (DefaultFiltersRunner.FILTER_FIELD_ID): new FilterField(rawValue: currentQuery, allowedNets: cl() as List<String>)
             ]))
         },
         filterMetadata: { cl ->
             String currentQuery = workflowService.findOne(filter.stringId).dataSet.get(DefaultFiltersRunner.FILTER_FIELD_ID).rawValue
             updateFilter(filter, new DataSet([
                     (DefaultFiltersRunner.FILTER_FIELD_ID): new FilterField(rawValue: currentQuery, filterMetadata: cl() as Map<String, Object>)
             ]))
         },
         title         : { cl ->
             filter = workflowService.findOne(filter.stringId)
             def value = cl()
             filter.setTitle(value as String)
             filter.dataSet.get(DefaultFiltersRunner.FILTER_I18N_TITLE_FIELD_ID).rawValue = (value instanceof I18nString) ? value : new I18nString(value as String)
             workflowService.save(filter)
         },
         icon          : { cl ->
             filter = workflowService.findOne(filter.stringId)
             def icon = cl() as String
             filter.setIcon(icon)
             workflowService.save(filter)
         },
         uri           : { cl ->
             filter = workflowService.findOne(filter.stringId)
             def uri = cl() as String
             filter.setUriNodeId(uriService.findByUri(uri).stringId)
             workflowService.save(filter)
         }]
    }

    /**
     * deletes filter instance
     * Note: do not call this method if given instance is referenced in any preference_item instance
     * @param filter
     * @return
     */
    def deleteFilter(Case filter) {
        workflowService.deleteCase(filter.stringId)
    }

    /**
     * change menu item attribute allowedRoles, bannedRoles or uri
     * usage:
     *       changeMenuItem item allowedRoles { newRoles }
     * @param item
     * @return
     */
    def changeMenuItem(Case item) {
        [allowedRoles          : { cl ->
            updateMenuItemRoles(item, cl as Closure, MenuItemConstants.PREFERENCE_ITEM_FIELD_ALLOWED_ROLES.attributeId)
        },
         bannedRoles           : { cl ->
             updateMenuItemRoles(item, cl as Closure, MenuItemConstants.PREFERENCE_ITEM_FIELD_BANNED_ROLES.attributeId)
         },
         caseDefaultHeaders    : { cl ->
             String defaultHeaders = cl() as String
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS.attributeId): new TextField(rawValue: defaultHeaders)
             ]))
         },
         taskDefaultHeaders    : { cl ->
             String defaultHeaders = cl() as String
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS.attributeId): new TextField(rawValue: defaultHeaders)
             ]))
         },
         filter                : { cl ->
             def filter = cl() as Case
             setData("change_filter", item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_NEW_FILTER_ID.attributeId): new TextField(rawValue: filter.stringId)
             ]))
         },
         uri                   : { cl ->
             def uri = cl() as String
             def aCase = useCase
             if (useCase == null || item.stringId != useCase.stringId) {
                 aCase = workflowService.findOne(item.stringId)
             }
             moveMenuItem(aCase, uri)
         },
         title                 : { cl ->
             def value = cl()
             I18nString newName = (value instanceof I18nString) ? value : new I18nString(value as String)
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_NAME.attributeId): new I18nField(rawValue: newName)
             ]))
         },
         menuIcon              : { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_ICON.attributeId): new TextField(rawValue: value)
             ]))
         },
         tabIcon               : { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_TAB_ICON.attributeId): new TextField(rawValue: value)
             ]))
         },
         requireTitleInCreation: { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_REQUIRE_TITLE_IN_CREATION.attributeId): new BooleanField(rawValue: value)
             ]))
         },
         useCustomView         : { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_CUSTOM_VIEW.attributeId): new BooleanField(rawValue: value)
             ]))
         },
         customViewSelector    : { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, new DataSet([
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_CUSTOM_VIEW_SELECTOR.attributeId): new TextField(rawValue: value)
             ]))
         }]
    }

    /**
     * Changes location of menu item. If non-existing location is provided, the new location is created and then the
     * item is moved. Cyclic destination path is forbidden (f.e. from <code>"/my_node"</code> to
     * <code>"/my_node/my_node2"</code>
     *
     * @param item Instance of preference_item to be moved
     * @param destUri destination path where the item will be moved. F.e. <code>"/my_new_node"</code>
     * */
    void moveMenuItem(Case item, String destUri) {
        if (isCyclicNodePath(item, destUri)) {
            throw new IllegalArgumentException("Cyclic path not supported. Destination path: ${destUri}")
        }

        List<Case> casesToSave = new ArrayList<>()

        List<String> parentIdList = item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue as ArrayList<String>
        if (parentIdList != null && parentIdList.size() > 0) {
            Case oldParent = removeChildItemFromParent(parentIdList[0], item)
            casesToSave.add(oldParent)
        }

        UriNode destNode = uriService.getOrCreate(destUri, UriContentType.CASE)
        Case newParent = getOrCreateFolderItem(destNode.uriPath)
        if (newParent != null) {
            item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue = [newParent.stringId] as ArrayList
            newParent = appendChildCaseId(newParent, item.stringId)
            casesToSave.add(newParent)
        } else {
            item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue = null
        }

        item.uriNodeId = destNode.stringId
        item = resolveAndHandleNewNodePath(item, destNode.uriPath)
        casesToSave.add(item)

        if (hasChildren(item)) {
            List<Case> childrenToSave = updateNodeInChildrenFoldersRecursive(item)
            casesToSave.addAll(childrenToSave)
        }

        for (aCase in casesToSave) {
            if (aCase != null) {
                workflowService.save(aCase)
            }
        }
    }

    protected Case getOrCreateFolderRecursive(UriNode node, MenuItemBody body, Case childFolderCase = null) {
        Case folder = findFolderCase(node)
        if (folder != null) {
            if (childFolderCase != null) {
                folder = appendChildCaseIdAndSave(folder, childFolderCase.stringId)
                initializeParentId(childFolderCase, folder.stringId)
            }
            return folder
        }

        folder = createCase(FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER, body.menuName.toString())
        folder.setUriNodeId(node.parentId)
        if (childFolderCase != null) {
            folder = appendChildCaseIdAndSave(folder, childFolderCase.stringId)
            initializeParentId(childFolderCase, folder.stringId)
        } else {
            folder = workflowService.save(folder)
        }
        Task newItemTask = findTask(folder.tasks.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_INIT_TRANS_ID.attributeId).taskStringId)
        assignTask(newItemTask)
        setData(newItemTask, body.toDataSet(null, node.uriPath))
        finishTask(newItemTask)

        folder = workflowService.findOne(folder.stringId)
        if (node.parentId != null) {
            UriNode parentNode = uriService.findById(node.parentId)
            body = new MenuItemBody(new I18nString(parentNode.name), "folder")

            getOrCreateFolderRecursive(parentNode, body, folder)
        }

        return folder
    }

    private Case appendChildCaseIdAndSave(Case folderCase, String childItemCaseId) {
        folderCase = appendChildCaseId(folderCase, childItemCaseId)
        return workflowService.save(folderCase)
    }


    private List<Case> updateNodeInChildrenFoldersRecursive(Case parentFolder) {
        List<String> childItemIds = parentFolder.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId].value as List<String>
        if (childItemIds == null || childItemIds.isEmpty()) {
            return new ArrayList<Case>()
        }

        List<Case> children = workflowService.findAllById(childItemIds)

        List<Case> casesToSave = new ArrayList<>()
        for (child in children) {
            UriNode parentNode = uriService.getOrCreate(parentFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId).rawValue as String, UriContentType.CASE)
            child.uriNodeId = parentNode.stringId
            child = resolveAndHandleNewNodePath(child, parentNode.uriPath)

            casesToSave.add(child)
            casesToSave.addAll(updateNodeInChildrenFoldersRecursive(child))
        }

        return casesToSave
    }

    private Case resolveAndHandleNewNodePath(Case folderItem, String destUri) {
        String newNodePath = resolveNewNodePath(folderItem, destUri)
        UriNode newNode = uriService.getOrCreate(newNodePath, UriContentType.CASE)
        folderItem.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId).rawValue = newNode.uriPath

        return folderItem
    }

    protected Case getOrCreateFolderItem(String uri) {
        UriNode node = uriService.getOrCreate(uri, UriContentType.CASE)
        MenuItemBody body = new MenuItemBody(new I18nString(node.name),"folder")
        return getOrCreateFolderRecursive(node, body)
    }

    private String resolveNewNodePath(Case folderItem, String destUri) {
        return destUri +
                uriService.getUriSeparator() +
                folderItem.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_IDENTIFIER.attributeId).rawValue as String
    }

    private Case removeChildItemFromParent(String folderId, Case childItem) {
        Case parentFolder = workflowService.findOne(folderId)
        (parentFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as List).remove(childItem.stringId)
        parentFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_HAS_CHILDREN.attributeId).rawValue = hasChildren(parentFolder)
        workflowService.save(parentFolder)
    }

    private boolean isCyclicNodePath(Case folderItem, String destUri) {
        String oldNodePath = folderItem.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId).rawValue
        return destUri.contains(oldNodePath)
    }

    // TODO: release/8.0.0 check why were missing
    protected String createNodePath(String uri, String identifier) {
        if (uri == uriService.getUriSeparator()) {
            return uri + identifier
        } else {
            return uri + uriService.getUriSeparator() + identifier
        }
    }

    protected String sanitize(String input) {
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[\\W-]+", "-")
                .toLowerCase()
    }

    // TODO: release/8.0.0: missing test
    private void updateMenuItemRoles(Case item, Closure cl, String roleFieldId) {
        item = workflowService.findOne(item.stringId)
        def roles = cl()
        MultichoiceMapField dataField = item.dataSet.get(roleFieldId) as MultichoiceMapField
        dataField.options = collectRolesForPreferenceItem(roles)
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

    /**
     * simplifies the process of creating a filter, menu item
     * @param uri
     * @param identifier - unique identifier of menu item
     * @param title
     * @param query
     * @param icon
     * @param type - "Case" or "Task"
     * @param allowedNets
     * @param allowedRoles
     * @param bannedRoles
     * @param visibility - "private" or "public"
     * @param orgGroup - group to add item to, if null default group is used
     * @return
     */
    @Deprecated
    Case createFilterInMenu(String uri, String identifier, def title, String query, String type, List<String> allowedNets,
                            List<String> allowedRoles = [],
                            List<String> bannedRoles = [],
                            List<String> defaultHeaders,
                            String icon = "",
                            String visibility = DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE,
                            Case orgGroup = null) {
        Case filter = createFilter(title, query, type, allowedNets, icon, visibility, null)
        Case menuItem = createMenuItem(uri, identifier, filter, allowedRoles, bannedRoles, orgGroup, defaultHeaders)
        return menuItem
    }

    Case createMenuItem(MenuItemBody body) {
        String sanitizedIdentifier = sanitize(body.identifier)

        if (existsMenuItem(sanitizedIdentifier)) {
            throw new IllegalArgumentException("Menu item identifier $sanitizedIdentifier is not unique!")
        }

        Case parentItemCase = getOrCreateFolderItem(body.uri)
        I18nString newName = body.menuName ?: (body.filter?.dataSet[FILTER_FIELD_I18N_FILTER_NAME].value as I18nString)

        Case menuItemCase = createCase(FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER, newName?.defaultValue)
        menuItemCase.setUriNodeId(uriService.findByUri(body.uri).stringId)
        menuItemCase.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_ALLOWED_ROLES.attributeId].options = body.allowedRoles
        menuItemCase.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_BANNED_ROLES.attributeId].options = body.bannedRoles
        if (parentItemCase != null) {
            parentItemCase = appendChildCaseIdAndSave(parentItemCase, menuItemCase.stringId)
        }
        menuItemCase = workflowService.save(menuItemCase)
        Task newItemTask = findTask { it.id.eq(new ObjectId(menuItemCase.getTaskStringId(MenuItemConstants.PREFERENCE_ITEM_FIELD_INIT_TRANS_ID.attributeId))) }
        String nodePath = createNodePath(body.uri, sanitizedIdentifier)
        uriService.getOrCreate(nodePath, UriContentType.CASE)

        newItemTask = assignTask(newItemTask)
        setData(newItemTask, body.toDataSet(parentItemCase.stringId, nodePath))
        finishTask(newItemTask)

        return workflowService.findOne(menuItemCase.stringId)
    }

    private Case appendChildCaseId(Case folderCase, String childItemCaseId) {
        List<String> childIds = folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as ArrayList<String>
        if (childIds == null) {
            folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue = [childItemCaseId] as ArrayList
        } else {
            folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue = childIds + [childItemCaseId] as ArrayList
        }

        folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_HAS_CHILDREN.attributeId).rawValue = hasChildren(folderCase)

        return folderCase
    }

    private boolean hasChildren(Case folderItem) {
        List<String> children = (folderItem.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId) as CaseField).rawValue
        return children != null && children.size() > 0
    }

    private Case initializeParentId(Case childFolderCase, String parentFolderCaseId) {
        childFolderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue = [parentFolderCaseId] as ArrayList
        return workflowService.save(childFolderCase)
    }

    protected Case findFolderCase(UriNode node) {
        return findCaseElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.nodePath.textValue.keyword:\"$node.uriPath\"")
    }

    /**
     * Finds filter by name
     *
     * @param name Title of the filter
     *
     * @return found filter instance. Can be null
     */
    Case findFilter(String name) {
        return findCaseElastic("processIdentifier:$FilterRunner.FILTER_PETRI_NET_IDENTIFIER AND title.keyword:\"$name\"" as String)
    }

    /**
     * Finds menu item by unique identifier
     *
     * @param menuItemIdentifier unique menu item identifier
     *
     * @return found preference_item instance. Can be null
     */
    Case findMenuItem(String menuItemIdentifier) {
        return findCaseElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.menu_item_identifier.textValue.keyword:\"$menuItemIdentifier\"" as String)
    }

    /**
     * Checks the menu item existence.
     *
     * @param menuItemIdentifier unique menu item identifier
     *
     * @return true if the item exists
     * */
    boolean existsMenuItem(String menuItemIdentifier) {
        return countCasesElastic("processIdentifier:\"$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER\" AND dataSet.menu_item_identifier.fulltextValue.keyword:\"$menuItemIdentifier\"") > 0
    }

    /**
     * find menu item by uri and name in default group
     * @param uri
     * @param name
     * @return
     */
    Case findMenuItem(String uri, String name) {
        UriNode uriNode = uriService.findByUri(uri)
        return findCaseElastic("processIdentifier:\"$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER\" AND title.keyword:\"$name\" AND uriNodeId:\"$uriNode.stringId\"")
    }

    Case findMenuItemByUriAndIdentifier(String uri, String identifier) {
        String nodePath = createNodePath(uri, identifier)
        return findCaseElastic("processIdentifier:\"$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER\" AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue.keyword:\"$nodePath\"")
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

    /**
     * Retrieves filter case from preference_item {@link Case}
     *
     * @param item preference_item instance
     *
     * @return found filter instance. If not found, <code>null</code> is returned
     */
    Case getFilterFromMenuItem(Case item) {
        String filterId = (item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_FILTER_CASE.attributeId).rawValue as List)[0] as String
        return filterId ? workflowService.findOne(filterId) : null
    }

    /**
     * search elastic with string query for first occurrence
     * @param query
     * @return
     */
    Case findCaseElastic(String query) {
        def result = findCasesElastic(query, PageRequest.of(0, 1))
        return result ? result[0] : null
    }

    /**
     * search elastic with string query for cases
     * @param query
     * @return
     */
    List<Case> findCasesElastic(String query, Pageable pageable) {
        CaseSearchRequest request = new CaseSearchRequest()
        request.query = query
        List<Case> result = elasticCaseService.search([request], userService.system.transformToLoggedUser(), pageable, LocaleContextHolder.locale, false).content
        return result
    }

    long countCasesElastic(String query) {
        CaseSearchRequest request = new CaseSearchRequest()
        request.query = query
        return elasticCaseService.count([request], userService.system.transformToLoggedUser(), LocaleContextHolder.locale, false)
    }

    @Deprecated
    private Case findMenuItemByUriNameProcessAndGroup(String uri, String name, Case orgGroup) {
        return findMenuItem(uri, name)
    }

    private Map<String, I18nString> collectRolesForPreferenceItem(List<String> roleImportIds) {
        List<ProcessRole> roles = roleService.findAllProcessRolesByImportIds(roleImportIds as Set<String>)
        return roles.collectEntries { role ->
            return [(role.importId), role.title]
        }
    }

    private void updateFilter(Case filter, DataSet dataSet) {
        setData(DefaultFiltersRunner.DETAILS_TRANSITION, filter, dataSet)
    }

    I18nString i18n(String value, Map<String, String> translations) {
        return new I18nString(value, translations)
    }

    void updateCase() {
        if (!useCase) {
            return
        }
        useCase = workflowService.findOne(useCase.stringId)
    }

    @Deprecated
    Map<String, Case> createMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, List<String> roles, List<String> bannedRoles = [], Case group = null, List<String> defaultHeaders = []) {
        if (existsMenuItem(id)) {
            log.info("$id menu exists")
            return null
        }
        Case filter = createCaseFilter(title, query, allowedNets, icon, DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE)
        Case menu = createMenuItem(uri, id, filter, roles, bannedRoles, group, defaultHeaders)
        return [
                "filter"  : filter,
                "menuItem": menu
        ]
    }

    @Deprecated
    Map<String, Case> createTaskMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles, Case group = null, List<String> defaultHeaders = []) {
        if (existsMenuItem(id)) {
            log.info("$id menu exists")
            return null
        }
        Case filter = createTaskFilter(title, query, allowedNets, icon, DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE)
        Case menu = createMenuItem(uri, id, filter, roles, [:], group, defaultHeaders)
        return [
                "filter"  : filter,
                "menuItem": menu
        ]
    }

    @Deprecated
    Case createOrUpdateCaseMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles = [:], Map<String, String> bannedRoles = [:], Case group = null, List<String> defaultHeaders = []) {
        return createOrUpdateMenuItemAndFilter(uri, id, title, query, DefaultFiltersRunner.FILTER_TYPE_CASE,
                DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE, allowedNets, icon, roles, bannedRoles, defaultHeaders)
    }

    @Deprecated
    Case createOrUpdateTaskMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles = [:], Map<String, String> bannedRoles = [:], Case group = null, List<String> defaultHeaders = []) {
        return createOrUpdateMenuItemAndFilter(uri, id, title, query, DefaultFiltersRunner.FILTER_TYPE_TASK,
                DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE, allowedNets, icon, roles, bannedRoles, defaultHeaders)
    }

    @Deprecated
    Case createOrUpdateMenuItem(String id, String uri, String type, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles = [:], Map<String, String> bannedRoles = [:], Case group = null, List<String> defaultHeaders = []) {
        Case menuItem = findMenuItem(sanitize(id))
        if (!menuItem) {
            Case filter = createFilter(title, query, type, allowedNets, icon, DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE, null)
            createUri(uri, UriContentType.DEFAULT)

            return createMenuItem(uri, id, title, icon, filter, roles, bannedRoles)
        } else {
            Case filter = getFilterFromMenuItem(menuItem)
            changeFilter filter query { query }
            changeFilter filter allowedNets { allowedNets }
            changeFilter filter title { title }
            changeFilter filter icon { icon }
            changeMenuItem menuItem allowedRoles { roles }
            changeMenuItem menuItem bannedRoles { bannedRoles }
            changeMenuItem menuItem defaultHeaders { defaultHeaders.join(",") }
            changeMenuItem menuItem uri { uri }
            changeMenuItem menuItem filter { filter }

            return workflowService.findOne(menuItem.stringId)
        }
    }

    /**
     * Creates or updates menu item with given identifier.
     *
     * @param uri resource where the item is located in
     * @param identifier unique identifier of item
     * @param name displayed label in menu and tab
     * @param icon displayed icon in menu and tab
     * @param filter Case instance of filter.xml
     * @param allowedRoles Map of roles, which have access to the item. Key is role_id in XML and value is process
     * identifier where the role exists
     * @param bannedRoles Map of roles, which don't have access to the item. Key is role_id in XML and value is process
     * identifier where the role exists
     * @param caseDefaultHeaders List of headers displayed in case view
     * @param taskDefaultHeaders List of headers displayed in task view
     *
     * @return created or updated menu item instance
     * */
    Case createOrUpdateMenuItem(String uri, String identifier, I18nString name, String icon = "filter_none", Case filter = null,
                                List<String> allowedRoles = [], List<String> bannedRoles = [],
                                List<String> caseDefaultHeaders = [], List<String> taskDefaultHeaders = []) {
        MenuItemBody body = new MenuItemBody(uri, identifier, name, icon)
        body.setAllowedRoles(collectRolesForPreferenceItem(allowedRoles))
        body.setBannedRoles(collectRolesForPreferenceItem(bannedRoles))
        body.setCaseDefaultHeaders(caseDefaultHeaders)
        body.setTaskDefaultHeaders(taskDefaultHeaders)
        body.setFilter(filter)

        return createOrUpdateMenuItem(body)
    }

    /**
     * Creates or updates menu item with given identifier along with the filter instance. It's safe to use on existing
     * menu item instance, that doesn't contain filter. In such case, missing filter will be created with provided
     * parameters.
     *
     * @param uri resource where the item is located in
     * @param itemIdentifier unique identifier of item
     * @param itemAndFilterName displayed label in menu and tab
     * @param filterQuery elastic query for filter
     * @param filterType type of filter. Possible values: {@value DefaultFiltersRunner#FILTER_TYPE_CASE} or
     * {@value DefaultFiltersRunner#FILTER_TYPE_TASK}
     * @param filterVisibility possible values: {@value DefaultFiltersRunner#FILTER_VISIBILITY_PRIVATE} or
     * {@value DefaultFiltersRunner#FILTER_VISIBILITY_PUBLIC}
     * @param filterAllowedNets List of allowed nets. Element of list is process identifier
     * @param itemAndFilterIcon displayed icon in menu and tab
     * @param itemAllowedRoles Map of roles, which have access to the item. Key is role_id in XML and value is process
     * identifier where the role exists
     * @param itemBannedRoles Map of roles, which don't have access to the item. Key is role_id in XML and value is process
     * identifier where the role exists
     * @param itemCaseDefaultHeaders List of headers displayed in case view
     * @param itemTaskDefaultHeaders List of headers displayed in task view
     * @param filterMetadata metadata for filter. If no value is provided, then default value is used: {@link #defaultFilterMetadata(String)}
     *
     * @return created or updated menu item instance along with the actual filter
     * */
    Case createOrUpdateMenuItemAndFilter(String uri, String itemIdentifier, I18nString itemAndFilterName, String filterQuery,
                                         String filterType, String filterVisibility, List<String> filterAllowedNets = [],
                                         String itemAndFilterIcon = "filter_none", List<String> itemAllowedRoles = [],
                                         List<String> itemBannedRoles = [], List<String> itemCaseDefaultHeaders = [],
                                         List<String> itemTaskDefaultHeaders = [], def filterMetadata = null) {
        MenuItemBody body = new MenuItemBody(uri, itemIdentifier, itemAndFilterName, itemAndFilterIcon)
        body.allowedRoles = collectRolesForPreferenceItem(itemAllowedRoles)
        body.bannedRoles = collectRolesForPreferenceItem(itemBannedRoles)
        body.caseDefaultHeaders = itemCaseDefaultHeaders
        body.taskDefaultHeaders = itemTaskDefaultHeaders

        return createOrUpdateMenuItemAndFilter(body, filterQuery, filterType, filterVisibility, filterAllowedNets,
                filterMetadata)
    }

    /**
     * Creates or updates menu item with given identifier.
     *
     * @param body data for menu item
     *
     * @return created or updated menu item instance
     * */
    Case createOrUpdateMenuItem(MenuItemBody body) {
        Case item = findMenuItem(sanitize(body.identifier))
        if (!item) {
            return createMenuItem(body)
        }
        return updateMenuItem(item, body)
    }

    /**
     * Creates or updates menu item with given identifier along with the filter instance. It's safe to use on existing
     * menu item instance, that doesn't contain filter. In such case, missing filter will be created with provided
     * parameters.
     *
     * @param body data for menu item
     * @param filterQuery elastic query for filter
     * @param filterType type of filter. Possible values: {@value DefaultFiltersRunner#FILTER_TYPE_CASE} or
     * {@value DefaultFiltersRunner#FILTER_TYPE_TASK}
     * @param filterVisibility possible values: {@value DefaultFiltersRunner#FILTER_VISIBILITY_PRIVATE} or
     * {@value DefaultFiltersRunner#FILTER_VISIBILITY_PUBLIC}
     * @param filterAllowedNets List of allowed nets. Element of list is process identifier
     * @param filterMetadata metadata for filter. If no value is provided, then default value is used: {@link #defaultFilterMetadata(String)}
     *
     * @return created or updated menu item instance along with the actual filter
     * */
    Case createOrUpdateMenuItemAndFilter(MenuItemBody body, String filterQuery, String filterType, String filterVisibility,
                                         List<String> filterAllowedNets = [], def filterMetadata = null) {
        Case item = findMenuItem(sanitize(body.identifier))
        if (item) {
            Case filter = getFilterFromMenuItem(item)
            if (filter) {
                changeFilter filter query { filterQuery }
                changeFilter filter visibility { filterVisibility }
                changeFilter filter allowedNets { filterAllowedNets }
                changeFilter filter filterMetadata { filterMetadata ?: defaultFilterMetadata(filterType) }
                changeFilter filter title { body.menuName }
                changeFilter filter icon { body.menuIcon }
            } else {
                body.filter = createFilter(body.menuName, filterQuery, filterType, filterAllowedNets, body.menuIcon,
                        filterVisibility, filterMetadata)
            }

            return updateMenuItem(item, body)
        } else {
            return createFilterInMenu(body, filterQuery, filterType, filterVisibility, filterAllowedNets, filterMetadata)
        }
    }

    /**
     * Creates menu item or ignores it if already exists
     *
     * @param body configuration class for menu item
     *
     * @return created or existing menu item instance
     * */
    Case createOrIgnoreMenuItem(MenuItemBody body) {
        Case item = findMenuItem(body.identifier)
        if (!item) {
            return createMenuItem(body)
        } else {
            return item
        }
    }

    /**
     * Creates menu item or ignores it if already exists. If existing item does not contain filter, the filter instance
     * is created by provided parameters.
     *
     * @param body configuration class for menu item
     *
     * @return created or existing menu item instance
     * */
    Case createOrIgnoreMenuItemAndFilter(MenuItemBody body, String filterQuery, String filterType, String filterVisibility,
                                         List<String> filterAllowedNets = [], def filterMetadata = null) {
        Case item = findMenuItem(body.identifier)
        if (!item) {
            return createFilterInMenu(body, filterQuery, filterType, filterVisibility, filterAllowedNets, filterMetadata)
        } else {
            Case filter = getFilterFromMenuItem(item)
            if (!filter) {
                filter = createFilter(body.menuName, filterQuery, filterType, filterAllowedNets, body.menuIcon, filterVisibility,
                        filterMetadata)
                changeMenuItem item filter { filter }
                return workflowService.findOne(item.stringId)
            } else {
                return item
            }
        }
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
        def outcome = setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, body.toDataSet())
        return outcome.case
    }

    static Map defaultFilterMetadata(String type) {
        return [
                "searchCategories"       : [],
                "predicateMetadata"      : [],
                "filterType"             : type,
                "defaultSearchCategories": true,
                "inheritAllowedNets"     : false
        ]
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
        return taskCase.getProcess().getDataSet().get(fieldId)
    }
}