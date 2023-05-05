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
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.application.engine.export.configuration.ExportConfiguration
import com.netgrif.application.engine.export.domain.ExportDataConfig
import com.netgrif.application.engine.export.service.interfaces.IExportService
import com.netgrif.application.engine.importer.service.FieldFactory
import com.netgrif.application.engine.mail.domain.MailDraft
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService
import com.netgrif.application.engine.mail.interfaces.IMailService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.pdf.generator.config.PdfResource
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfGenerator
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.Transition
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.DynamicValidation
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.version.Version
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.rules.domain.RuleRepository
import com.netgrif.application.engine.startup.DefaultFiltersRunner
import com.netgrif.application.engine.startup.FilterRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome
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

import java.time.ZoneId
import java.util.stream.Collectors

/**
 * ActionDelegate class contains Actions API methods.
 */
@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class ActionDelegate {

    static final Logger log = LoggerFactory.getLogger(ActionDelegate)

    private static final String PREFERENCE_ITEM_FIELD_NEW_FILTER_ID = "new_filter_id"
    private static final String PREFERENCE_ITEM_FIELD_REMOVE_OPTION = "remove_option"
    private static final String PREFERENCE_ITEM_FIELD_FILTER_CASE = "filter_case"
    private static final String PREFERENCE_ITEM_FIELD_PARENTID = "parentId"
    private static final String PREFERENCE_ITEM_FIELD_DEFAULT_HEADERS = "default_headers"
    private static final String PREFERENCE_ITEM_FIELD_IDENTIFIER = "menu_item_identifier"
    private static final String PREFERENCE_ITEM_FIELD_APPEND_MENU_ITEM = "append_menu_item_stringId"
    private static final String PREFERENCE_ITEM_FIELD_ALLOWED_ROLES = "allowed_roles"
    private static final String PREFERENCE_ITEM_FIELD_BANNED_ROLES = "banned_roles"
    private static final String ORG_GROUP_FIELD_FILTER_TASKS = "filter_tasks"

    static final String UNCHANGED_VALUE = "unchangedooo"
    static final String ALWAYS_GENERATE = "always"
    static final String ONCE_GENERATE = "once"
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
    IPdfGenerator pdfGenerator

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
    IDataValidationExpressionEvaluator dataValidationExpressionEvaluator

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

    /**
     * Reference of case and task in which current action is taking place.
     */
    Case useCase
    Optional<Task> task
    def map = [:]
    Action action
    FieldActionsRunner actionsRunner
    List<EventOutcome> outcomes

    def init(Action action, Case useCase, Optional<Task> task, FieldActionsRunner actionsRunner) {
        this.action = action
        this.useCase = useCase
        this.task = task
        this.actionsRunner = actionsRunner
        this.initFieldsMap(action.fieldIds)
        this.initTransitionsMap(action.transitionIds)
        this.outcomes = new ArrayList<>()
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

    def copyBehavior(Field field, Transition transition) {
        if (!useCase.hasFieldBehavior(field.stringId, transition.stringId)) {
            useCase.dataSet.get(field.stringId).addBehavior(transition.stringId, transition.dataSet.get(field.stringId).behavior)
        }
    }

    def visible = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeVisible(trans.stringId)
    }

    def editable = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeEditable(trans.stringId)
    }

    def required = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeRequired(trans.stringId)
    }

    def optional = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeOptional(trans.stringId)
    }

    def hidden = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeHidden(trans.stringId)
    }

    def forbidden = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeForbidden(trans.stringId)
    }

    def initial = { Field field, Transition trans ->
        useCase.petriNet.transitions.get(trans.stringId).dataSet.get(field.stringId).behavior
    }

    def unchanged = { return UNCHANGED_VALUE }

    def initValueOfField = { Field field ->
        if (!field.hasDefault()) {
            return null
        } else if (field.isDynamicDefaultValue()) {
            return initValueExpressionEvaluator.evaluate(useCase, field)
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
     * This code will change the behavior of fields <i>text</i> and <i>anotherText</i> to <i>visible</i> on each transition that contains given fields when field's <i>condition</i> value is equal to <i>true</i>.
     * @param list of fields which behaviour will be changed
     * @param behavior one of initial, visible, editable, required, optional, hidden, forbidden
     */
    def make(List<Field> fields, Closure behavior) {
        def behaviorClosureResult

        [on: { Object transitionObject ->
            [when: { Closure condition ->
                if (condition()) {
                    if (transitionObject instanceof Transition) {
                        fields.forEach { field ->
                            behaviorClosureResult = behavior(field, transitionObject)
                            saveFieldBehavior(field, transitionObject, (behavior == initial) ? behaviorClosureResult as Set : null)
                        }
                    } else if (transitionObject instanceof List<?>) {
                        transitionObject.each { trans ->
                            if (trans instanceof Transition) {
                                fields.each { field ->
                                    if (trans.dataSet.containsKey(field.stringId)) {
                                        behaviorClosureResult = behavior(field, trans)
                                        saveFieldBehavior(field, trans, (behavior == initial) ? behaviorClosureResult as Set : null)
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
                                        saveFieldBehavior(field, trans, (behavior == initial) ? behaviorClosureResult as Set : null)
                                    }
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("Invalid call of make method. Method call should contain specific transition (transitions) or keyword \'transitions\'.")
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid call of make method. Method call should contain specific transition (transitions) or keyword \'transitions\'.")
                    }
                }
            }]
        }]
    }

    protected SetDataEventOutcome createSetDataEventOutcome() {
        return new SetDataEventOutcome(this.useCase, this.task.orElse(null))
    }

    def saveFieldBehavior(Field field, Transition trans, Set<FieldBehavior> initialBehavior) {
        Map<String, Set<FieldBehavior>> fieldBehavior = useCase.dataSet.get(field.stringId).behavior
        if (initialBehavior != null)
            fieldBehavior.put(trans.stringId, initialBehavior)

        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("type", field.type.name)
        changedField.addBehavior(fieldBehavior)
        SetDataEventOutcome outcome = createSetDataEventOutcome()
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedChoices(ChoiceField field) {
        useCase.dataSet.get(field.stringId).choices = field.choices
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("choices", field.choices.collect { it.getTranslation(LocaleContextHolder.locale) })
        SetDataEventOutcome outcome = createSetDataEventOutcome()
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedAllowedNets(CaseField field) {
        useCase.dataSet.get(field.stringId).allowedNets = field.allowedNets
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("allowedNets", field.allowedNets)
        SetDataEventOutcome outcome = createSetDataEventOutcome()
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedOptions(MapOptionsField field) {
        useCase.dataSet.get(field.stringId).options = field.options
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("options", field.options.collectEntries { key, value -> [key, (value as I18nString).getTranslation(LocaleContextHolder.locale)] })
        SetDataEventOutcome outcome = createSetDataEventOutcome()
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedValidation(Field field) {
        useCase.dataSet.get(field.stringId).validations = field.validations
        List<Validation> compiled = field.validations.collect { it.clone() }
        compiled.findAll { it instanceof DynamicValidation }.collect { (DynamicValidation) it }.each {
            it.compiledRule = dataValidationExpressionEvaluator.compile(useCase, it.expression)
        }
        ChangedField changedField = new ChangedField(field.stringId)
        changedField.addAttribute("validations", compiled.collect { it.getLocalizedValidation(LocaleContextHolder.locale) })
        SetDataEventOutcome outcome = createSetDataEventOutcome()
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
                 executeTasks(dataSet, taskId, closure)
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

    def change(Field field) {
        [about      : { cl -> // TODO: deprecated
            changeFieldValue(field, cl)
        },
         value      : { cl ->
             changeFieldValue(field, cl)
         },
         choices    : { cl ->
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
             saveChangedChoices(field)
         },
         allowedNets: { cl ->
             if (!(field instanceof CaseField)) // TODO make this work with FilterField as well
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
             saveChangedAllowedNets(field)
         },
         options    : { cl ->
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
                     Map<String, I18nString> newOptions = new LinkedHashMap<>();
                     options.each { it -> newOptions.put(it.getKey() as String, new I18nString(it.getValue() as String)) }
                     field.setOptions(newOptions)
                 }
                 saveChangedOptions(field)
             } else if (field instanceof ChoiceField) {
                 field = (ChoiceField) field
                 if (options.every { it.getValue() instanceof I18nString }) {
                     Set<I18nString> choices = new LinkedHashSet<>()
                     options.forEach({ k, v -> choices.add(v) })
                     field.setChoices(choices)
                 } else {
                     Set<I18nString> newChoices = new LinkedHashSet<>();
                     options.each { it -> newChoices.add(new I18nString(it.getValue() as String)) }
                     field.setChoices(newChoices)
                 }
                 saveChangedChoices(field)
             }

         },
         validations: { cl ->
             changeFieldValidations(field, cl)
         }
        ]
    }

    void changeFieldValue(Field field, def cl) {
        def value = cl()
        if (value instanceof Closure) {
            if (value == initValueOfField) {
                value = initValueOfField(field)

            } else if (value() == UNCHANGED_VALUE) {
                return
            }
        }
        if (value == null && useCase.dataSet.get(field.stringId).value != null) {
            if (field instanceof FileListField && task.isPresent()) {
                field.value.namesPaths.forEach(namePath -> {
                    dataService.deleteFileByName(task.get().stringId, field.stringId, namePath.name)
                })
            }
            if (field instanceof FileField && task.isPresent()) {
                dataService.deleteFile(task.get().stringId, field.stringId)
            }
            field.clearValue()
            saveChangedValue(field)
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
                value.each {id -> users.add(new UserFieldValue(userService.findById(id as String, false)))}
                value = new UserListFieldValue(users)
            }
            field.value = value
            saveChangedValue(field)
        }

        useCase = dataService.applyFieldConnectedChanges(useCase, field)
        ChangedField changedField = new ChangedField(field.stringId)
        if (field instanceof I18nField) {
            changedField.attributes.put("value", value)
        } else {
            changedField.addAttribute("value", value)
        }
        changedField.addAttribute("type", field.type.name)
        SetDataEventOutcome outcome = createSetDataEventOutcome()
        outcome.addChangedField(field.stringId, changedField)
        this.outcomes.add(outcome)
    }

    def saveChangedValue(Field field) {
        useCase.dataSet.get(field.stringId).value = field.value
    }

    void changeFieldValidations(Field field, def cl) {
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
        saveChangedValidation(field)
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

    Case findCase(Closure<Predicate> predicate) {
        QCase qCase = new QCase("case")
        return workflowService.searchOne(predicate(qCase))
    }

    Case createCase(String identifier, String title = null, String color = "", IUser author = userService.loggedOrSystem, Locale locale = LocaleContextHolder.getLocale()) {
        return workflowService.createCaseByIdentifier(identifier, title, color, author.transformToLoggedUser(), locale).getCase()
    }

    Case createCase(PetriNet net, String title = net.defaultCaseName.getTranslation(locale), String color = "", IUser author = userService.loggedOrSystem, Locale locale = LocaleContextHolder.getLocale()) {
        CreateCaseEventOutcome outcome = workflowService.createCase(net.stringId, title, color, author.transformToLoggedUser())
        this.outcomes.add(outcome)
        return outcome.getCase()
    }

    Task assignTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem) {
        String taskId = getTaskId(transitionId, aCase)
        AssignTaskEventOutcome outcome = taskService.assignTask(user.transformToLoggedUser(), taskId)
        this.outcomes.add(outcome)
        return outcome.getTask()
    }

    Task assignTask(Task task, IUser user = userService.loggedOrSystem) {
        return addTaskOutcomeAndReturnTask(taskService.assignTask(task, user))
    }

    void assignTasks(List<Task> tasks, IUser assignee = userService.loggedOrSystem) {
        this.outcomes.addAll(taskService.assignTasks(tasks, assignee))
    }

    Task cancelTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem) {
        String taskId = getTaskId(transitionId, aCase)
        return addTaskOutcomeAndReturnTask(taskService.cancelTask(user.transformToLoggedUser(), taskId))
    }

    Task cancelTask(Task task, IUser user = userService.loggedOrSystem) {
        return addTaskOutcomeAndReturnTask(taskService.cancelTask(task, user))
    }

    void cancelTasks(List<Task> tasks, IUser user = userService.loggedOrSystem) {
        this.outcomes.addAll(taskService.cancelTasks(tasks, user))
    }

    private Task addTaskOutcomeAndReturnTask(TaskEventOutcome outcome) {
        this.outcomes.add(outcome)
        return outcome.getTask()
    }

    void finishTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem) {
        String taskId = getTaskId(transitionId, aCase)
        addTaskOutcomeAndReturnTask(taskService.finishTask(user.transformToLoggedUser(), taskId))
    }

    void finishTask(Task task, IUser user = userService.loggedOrSystem) {
        addTaskOutcomeAndReturnTask(taskService.finishTask(task, user))
    }

    void finishTasks(List<Task> tasks, IUser finisher = userService.loggedOrSystem) {
        this.outcomes.addAll(taskService.finishTasks(tasks, finisher))
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
        return taskService.searchOne(QTask.task._id.eq(new ObjectId(mongoId)))
    }

    String getTaskId(String transitionId, Case aCase = useCase) {
        List<TaskReference> refs = taskService.findAllByCase(aCase.stringId, null)
        refs.find { it.transitionId == transitionId }.stringId
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

    SetDataEventOutcome setData(Task task, Map dataSet) {
        return setData(task.stringId, dataSet)
    }

    SetDataEventOutcome setData(String taskId, Map dataSet) {
        return addSetDataOutcomeToOutcomes(dataService.setData(taskId, ImportHelper.populateDataset(dataSet)))
    }

    SetDataEventOutcome setData(Transition transition, Map dataSet) {
        return addSetDataOutcomeToOutcomes(setData(transition.importId, this.useCase, dataSet))
    }

    SetDataEventOutcome setData(String transitionId, Case useCase, Map dataSet) {
        def predicate = QTask.task.caseId.eq(useCase.stringId) & QTask.task.transitionId.eq(transitionId)
        def task = taskService.searchOne(predicate)
        return addSetDataOutcomeToOutcomes(dataService.setData(task.stringId, ImportHelper.populateDataset(dataSet)))
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

    Map<String, Field> getData(Task task) {
        def useCase = workflowService.findOne(task.caseId)
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase)))
    }

    Map<String, Field> getData(String taskId) {
        Task task = taskService.findById(taskId)
        def useCase = workflowService.findOne(task.caseId)
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase)))
    }

    Map<String, Field> getData(Transition transition) {
        return getData(transition.stringId, this.useCase)
    }

    Map<String, Field> getData(String transitionId, Case useCase) {
        def predicate = QTask.task.caseId.eq(useCase.stringId) & QTask.task.transitionId.eq(transitionId)
        def task = taskService.searchOne(predicate)
        if (!task)
            return new HashMap<String, Field>()
        return mapData(addGetDataOutcomeToOutcomesAndReturnData(dataService.getData(task, useCase)))
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
        MailDraft mailDraft = MailDraft.builder(mailFrom, to).subject(subject).body(body).build();
        sendMail(mailDraft)
    }

    void sendEmail(List<String> to, String subject, String body, Map<String, File> attachments) {
        MailDraft mailDraft = MailDraft.builder(mailFrom, to).subject(subject).body(body).attachments(attachments).build();
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
        IUser user = registrationService.createNewUser(newUserRequest);
        if (user == null)
            return MessageResource.successMessage("Done");
        mailService.sendRegistrationEmail(user);

        mailAttemptService.mailAttempt(newUserRequest.email);
        return MessageResource.successMessage("Done");
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
        QTask qTask = new QTask("task");
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
        return uriService.findByUri(uri);
    }

    def createUri(String uri, UriContentType type) {
        return uriService.getOrCreate(uri, type)
    }

    def moveUri(String uri, String dest) {
        return uriService.move(uri, dest)
    }

    List<Case> findDefaultFilters() {
        if (!createDefaultFilters) {
            return []
        }
        return findCases({ it.processIdentifier.eq(FilterRunner.FILTER_PETRI_NET_IDENTIFIER).and(it.author.id.eq(userService.system.stringId)) })
    }

    /**
     * create filter instance of type Case, to create a menu item call {@link #createMenuItem()}
     * @param title
     * @param query
     * @param icon
     * @param allowedNets
     * @param visibility "private" or "public"
     * @param filterMetadata
     * @return
     */
    @NamedVariant
    Case createCaseFilter(def title, String query, List<String> allowedNets,
                          String icon = "", String visibility = DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE, def filterMetadata = null) {
        return createFilter(title, query, DefaultFiltersRunner.FILTER_TYPE_CASE, allowedNets, icon, visibility, filterMetadata)
    }

    /**
     * create filter instance of type Task, to create a menu item call {@link #createMenuItem()}
     * @param title
     * @param query
     * @param icon
     * @param allowedNets
     * @param visibility "private" or "public"
     * @param filterMetadata
     * @return
     */
    @NamedVariant
    Case createTaskFilter(def title, String query, List<String> allowedNets,
                          String icon = "", String visibility = DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE, def filterMetadata = null) {
        return createFilter(title, query, DefaultFiltersRunner.FILTER_TYPE_TASK, allowedNets, icon, visibility, filterMetadata)
    }

    /**
     * create filter instance, to create a menu item call {@link #createMenuItem()}
     * @param title
     * @param query
     * @param icon
     * @param type "Case" or "Task"
     * @param allowedNets
     * @param visibility "private" or "public"
     * @param filterMetadata
     * @return
     */
    @NamedVariant
    Case createFilter(def title, String query, String type, List<String> allowedNets,
                      String icon, String visibility, def filterMetadata) {
        Case filterCase = createCase(FilterRunner.FILTER_PETRI_NET_IDENTIFIER, title as String)
        filterCase.setIcon(icon)
        filterCase.dataSet[DefaultFiltersRunner.FILTER_I18N_TITLE_FIELD_ID].value = (title instanceof I18nString) ? title : new I18nString(title as String)
        filterCase = workflowService.save(filterCase)
        Task newFilterTask = findTask { it._id.eq(new ObjectId(filterCase.tasks.find { it.transition == DefaultFiltersRunner.AUTO_CREATE_TRANSITION }.task)) }
        assignTask(newFilterTask)

        def setDataMap = [
                (DefaultFiltersRunner.FILTER_TYPE_FIELD_ID)      : [
                        "type" : "enumeration_map",
                        "value": type
                ],
                (DefaultFiltersRunner.FILTER_VISIBILITY_FIELD_ID): [
                        "type" : "enumeration_map",
                        "value": visibility
                ],
                (DefaultFiltersRunner.FILTER_FIELD_ID)           : [
                        "type"          : "filter",
                        "value"         : query,
                        "allowedNets"   : allowedNets,
                        "filterMetadata": filterMetadata ?: [
                                "searchCategories"       : [],
                                "predicateMetadata"      : [],
                                "filterType"             : type,
                                "defaultSearchCategories": true,
                                "inheritAllowedNets"     : false
                        ]
                ]
        ]
        setData(newFilterTask, setDataMap)
        finishTask(newFilterTask)
        return workflowService.findOne(filterCase.stringId)
    }

    /**
     * Change filter instance attribute; query, visibility ("public"/"private"), title, allowedNets, filterMetadata or uri
     * if filter is referenced within a menu item, reload said menu item using
     * changeMenuItem item filter { filter }
     * @param filter
     * @return
     */
    def changeFilter(Case filter) {
        [query         : { cl ->
            updateFilter(filter, [
                    (DefaultFiltersRunner.FILTER_FIELD_ID): [
                            "type" : "enumeration_map",
                            "value": cl() as String
                    ]
            ])
        },
         visibility    : { cl ->
             updateFilter(filter, [
                     (DefaultFiltersRunner.FILTER_VISIBILITY_FIELD_ID): [
                             "type" : "enumeration_map",
                             "value": cl() as String
                     ]
             ])
         },
         allowedNets   : { cl ->
             String currentQuery = workflowService.findOne(filter.stringId).dataSet[DefaultFiltersRunner.FILTER_FIELD_ID].value
             updateFilter(filter, [
                     (DefaultFiltersRunner.FILTER_FIELD_ID): [
                             "type"       : "filter",
                             "value"      : currentQuery,
                             "allowedNets": cl() as List<String>
                     ]
             ])
         },
         filterMetadata: { cl ->
             String currentQuery = workflowService.findOne(filter.stringId).dataSet[DefaultFiltersRunner.FILTER_FIELD_ID].value
             updateFilter(filter, [
                     (DefaultFiltersRunner.FILTER_FIELD_ID): [
                             "type"          : "filter",
                             "value"         : currentQuery,
                             "filterMetadata": cl() as Map<String, Object>
                     ]
             ])
         },
         title         : { cl ->
             filter = workflowService.findOne(filter.stringId)
             def value = cl()
             filter.setTitle(value as String)
             filter.dataSet[DefaultFiltersRunner.FILTER_I18N_TITLE_FIELD_ID].value = (value instanceof I18nString) ? value : new I18nString(value as String)
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
             filter.setUriNodeId(uriService.findByUri(uri).id)
             workflowService.save(filter)
         }]
    }

    /**
     * deletes filter instance
     * Note: do not call this method if given instance is references in any preference_filter_item instance
     * @param filter
     * @return
     */
    def deleteFilter(Case filter) {
        workflowService.deleteCase(filter.stringId)
    }

    /**
     * create menu item for given filter instance
     * @param uri
     * @param identifier - unique item identifier
     * @param filter
     * @param groupName
     * @param allowedRoles ["role_import_id": "net_import_id"]
     * @param bannedRoles ["role_import_id": "net_import_id"]
     * @return
     */
    Case createMenuItem(String uri, String identifier, Case filter, String groupName, Map<String, String> allowedRoles, Map<String, String> bannedRoles = [:], List<String> defaultHeaders = []) {
        return doCreateMenuItem(uri, identifier, filter, nextGroupService.findByName(groupName), collectRolesForPreferenceItem(allowedRoles), collectRolesForPreferenceItem(bannedRoles), defaultHeaders)
    }

    /**
     * create menu item for given filter instance
     * @param uri
     * @param identifier - unique item identifier
     * @param filter
     * @param groupName
     * @param allowedRoles
     * @param bannedRoles
     * @return
     */
    Case createMenuItem(String uri, String identifier, Case filter, String groupName, List<ProcessRole> allowedRoles, List<ProcessRole> bannedRoles = [], List<String> defaultHeaders = []) {
        return doCreateMenuItem(uri, identifier, filter, nextGroupService.findByName(groupName), collectRolesForPreferenceItem(allowedRoles), collectRolesForPreferenceItem(bannedRoles), defaultHeaders)
    }

    /**
     * create menu item for given filter instance
     * @param uri
     * @param identifier - unique item identifier
     * @param filter
     * @param groupName
     * @param allowedRoles ["role_import_id": "net_import_id"]
     * @param bannedRoles ["role_import_id": "net_import_id"]
     * @param group - if null, default group is used
     * @param group - if null, default group is used
     * @return
     */
    Case createMenuItem(String uri, String identifier, Case filter, Map<String, String> allowedRoles, Map<String, String> bannedRoles = [:], Case group = null, List<String> defaultHeaders = []) {
        return doCreateMenuItem(uri, identifier, filter, group, collectRolesForPreferenceItem(allowedRoles), collectRolesForPreferenceItem(bannedRoles), defaultHeaders)
    }

    /**
     * create menu item for given filter instance
     * @param uri
     * @param identifier - unique item identifier
     * @param filter
     * @param allowedRoles
     * @param bannedRoles
     * @param group - if null, default group is used
     * @return
     */
    Case createMenuItem(String uri, String identifier, Case filter, List<ProcessRole> allowedRoles, List<ProcessRole> bannedRoles = [], Case group = null, List<String> defaultHeaders = []) {
        return doCreateMenuItem(uri, identifier, filter, group, collectRolesForPreferenceItem(allowedRoles), collectRolesForPreferenceItem(bannedRoles), defaultHeaders)
    }

    /**
     * change menu item attribute allowedRoles, bannedRoles or uri
     * usage:
     *       changeMenuItem item allowedRoles { newRoles }
     * @param item
     * @return
     */
    def changeMenuItem(Case item) {
        [allowedRoles: { cl ->
            updateMenuItemRoles(item, cl as Closure, PREFERENCE_ITEM_FIELD_ALLOWED_ROLES)
        },
         bannedRoles : { cl ->
             updateMenuItemRoles(item, cl as Closure, PREFERENCE_ITEM_FIELD_BANNED_ROLES)
         },
         defaultHeaders : {  cl ->
             String defaultHeaders = cl() as String
             setData("view", item, [
                     (PREFERENCE_ITEM_FIELD_DEFAULT_HEADERS): ["type": "text", "value": defaultHeaders]
             ])
             workflowService.save(item)
         },
         filter      : { cl ->
             def filter = cl() as Case
             setData("change_filter", item, [
                     (PREFERENCE_ITEM_FIELD_NEW_FILTER_ID): ["type": "text", "value": filter.stringId]
             ])
         },
         uri         : { cl ->
             item = workflowService.findOne(item.stringId)
             def uri = cl() as String
             item.setUriNodeId(uriService.findByUri(uri).id)
             workflowService.save(item)
         }]
    }

    private void updateMenuItemRoles(Case item, Closure cl, String roleFieldId) {
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
        def task = item.tasks.find { it.transition == "view" }.task
        dataService.setData(task, ImportHelper.populateDataset([
                (PREFERENCE_ITEM_FIELD_REMOVE_OPTION): ["type": "button", "value": 0]
        ]))
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
     * @param groupName - name of group to add menu item to
     * @param allowedRoles
     * @param bannedRoles
     * @param visibility - "private" or "public"
     * @return
     */
    Case createFilterInMenu(String uri, String identifier, def title, String query, String type,
                            List<String> allowedNets,
                            String groupName,
                            Map<String, String> allowedRoles = [:],
                            Map<String, String> bannedRoles = [:],
                            List<String> defaultHeaders = [],
                            String icon = "",
                            String visibility = DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE) {
        Case filter = createFilter(title, query, type, allowedNets, icon, visibility, null)
        Case menuItem = createMenuItem(uri, identifier, filter, groupName, allowedRoles, bannedRoles, defaultHeaders)
        return menuItem
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
    Case createFilterInMenu(String uri, String identifier, def title, String query, String type, List<String> allowedNets,
                            Map<String, String> allowedRoles = [:],
                            Map<String, String> bannedRoles = [:],
                            List<String> defaultHeaders,
                            String icon = "",
                            String visibility = DefaultFiltersRunner.FILTER_VISIBILITY_PRIVATE,
                            Case orgGroup = null) {
        Case filter = createFilter(title, query, type, allowedNets, icon, visibility, null)
        Case menuItem = createMenuItem(uri, identifier, filter, allowedRoles, bannedRoles, orgGroup, defaultHeaders)
        return menuItem
    }

    protected Case doCreateMenuItem(String uri, String identifier, Case filter, Case orgGroup, Map<String, I18nString> allowedRoles, Map<String, I18nString> bannedRoles, List<String> defaultHeaders) {
        if (findMenuItem(identifier)) {
            throw new IllegalArgumentException("Menu item identifier $identifier is not unique!")
        }
        orgGroup = orgGroup ?: nextGroupService.findDefaultGroup()
        Case itemCase = createCase(FilterRunner.PREFERRED_FILTER_ITEM_NET_IDENTIFIER, filter.title)
        itemCase.setUriNodeId(uriService.findByUri(uri).id)
        itemCase.dataSet[PREFERENCE_ITEM_FIELD_ALLOWED_ROLES].options = allowedRoles
        itemCase.dataSet[PREFERENCE_ITEM_FIELD_BANNED_ROLES].options = bannedRoles
        itemCase = workflowService.save(itemCase)
        Task newItemTask = findTask { it._id.eq(new ObjectId(itemCase.tasks.find { it.transition == "init" }.task)) }
        assignTask(newItemTask)
        def setDataMap = [
                (PREFERENCE_ITEM_FIELD_FILTER_CASE)    : [
                        "type" : "caseRef",
                        "value": [filter.stringId]
                ],
                (PREFERENCE_ITEM_FIELD_PARENTID)       : [
                        "type" : "text",
                        "value": orgGroup.stringId
                ],
                (PREFERENCE_ITEM_FIELD_DEFAULT_HEADERS): [
                        "type" : "text",
                        "value": defaultHeaders.join(',')
                ],
                (PREFERENCE_ITEM_FIELD_IDENTIFIER)     : [
                        "type" : "text",
                        "value": identifier
                ],
        ]
        setData(newItemTask, setDataMap)
        finishTask(newItemTask)

        def task = orgGroup.tasks.find { it.transition == "append_menu_item" }.task
        dataService.setData(task, ImportHelper.populateDataset([
                (PREFERENCE_ITEM_FIELD_APPEND_MENU_ITEM): ["type": "text", "value": itemCase.stringId]
        ]))

        return workflowService.findOne(itemCase.stringId)
    }

    /**
     * find filter by uri and title
     * @param uri
     * @param name
     * @return
     */
    Case findFilter(String name) {
        return findCaseElastic("processIdentifier:$FilterRunner.FILTER_PETRI_NET_IDENTIFIER AND title.keyword:\"$name\"" as String)
    }

    /**
     * find menu item by unique identifier
     * @param name
     * @return
     */
    Case findMenuItem(String menuItemIdentifier) {
        return findCaseElastic("processIdentifier:$FilterRunner.PREFERRED_FILTER_ITEM_NET_IDENTIFIER AND dataSet.menu_item_identifier.textValue.keyword:\"$menuItemIdentifier\"" as String)
    }

    /**
     * find menu item by uri and name in default group
     * @param uri
     * @param name
     * @return
     */
    Case findMenuItem(String uri, String name) {
        return findMenuItemInGroup(uri, name, null)
    }

    /**
     * find menu item by uri, title and name of group
     * @param uri
     * @param name
     * @param groupName
     * @return
     */
    Case findMenuItem(String uri, String name, String groupName) {
        Case orgGroup = nextGroupService.findByName(groupName)
        return findMenuItemInGroup(uri, name, orgGroup)
    }

    /**
     *
     * @param uri
     * @param name
     * @param orgGroup
     * @return
     */
    Case findMenuItemInGroup(String uri, String name, Case orgGroup) {
        return findMenuItemByUriNameProcessAndGroup(uri, name, orgGroup)
    }

    /**
     * Retrieve filter case from preference_filter_item case
     * @param item
     * @return
     */
    Case getFilterFromMenuItem(Case item) {
        return workflowService.findOne((item.dataSet[PREFERENCE_ITEM_FIELD_FILTER_CASE].value as List)[0] as String)
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

    private Case findMenuItemByUriNameProcessAndGroup(String uri, String name, Case orgGroup) {
        UriNode uriNode = uriService.findByUri(uri)
        if (!orgGroup) {
            return uriNode ? findCaseElastic("processIdentifier:\"$FilterRunner.PREFERRED_FILTER_ITEM_NET_IDENTIFIER\" AND title.keyword:\"$name\" AND uriNodeId:\"$uriNode.id\"") : null
        }
        List<String> taskIds = (orgGroup.dataSet[ORG_GROUP_FIELD_FILTER_TASKS].value ?: []) as List
        if (!taskIds) {
            return null
        }
        List<Task> tasks = taskService.findAllById(taskIds)
        List<Case> preferenceItemsOfGroup = workflowService.findAllById(tasks.collect { it.stringId })
        return preferenceItemsOfGroup.find { it.title == name && it.uriNodeId == uriNode.id }
    }

    private Map<String, I18nString> collectRolesForPreferenceItem(List<ProcessRole> roles) {
        return roles.collectEntries { role ->
            PetriNet net = petriNetService.get(new ObjectId(role.netId))
            return [(role.importId + ":" + net.identifier), ("$role.name ($net.title)" as String)]
        } as Map<String, I18nString>
    }

    private Map<String, I18nString> collectRolesForPreferenceItem(Map<String, String> roles) {
        Map<String, PetriNet> temp = [:]
        return roles.collectEntries { entry ->
            if (!temp.containsKey(entry.value)) {
                temp.put(entry.value, petriNetService.getNewestVersionByIdentifier(entry.value))
            }
            PetriNet net = temp[entry.value]
            ProcessRole role = net.roles.find { it.value.importId == entry.key }.value
            return [(role.importId + ":" + net.identifier), ("$role.name ($net.title)" as String)]
        } as Map<String, I18nString>
    }

    private void updateFilter(Case filter, Map dataSet) {
        setData(DefaultFiltersRunner.DETAILS_TRANSITION, filter, dataSet)
    }

    I18nString i18n(String value, Map<String, String> translations) {
        return new I18nString(value, translations)
    }

    Map<String, Case> createMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles, Map<String, String> bannedRoles = [:], Case group = null, List<String> defaultHeaders = []) {
        if (findMenuItem(id)) {
            log.info("$id menu exists")
            return
        }
        Case filter = createCaseFilter(title, query, allowedNets, icon, "private")
        Case menu = createMenuItem(uri, id, filter, roles, bannedRoles, group, defaultHeaders)
        return [
                "filter"  : filter,
                "menuItem": menu
        ];
    }

    Map<String, Case> createTaskMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles, Case group = null, List<String> defaultHeaders = []) {
        if (findMenuItem(id)) {
            log.info("$id menu exists")
            return
        }
        Case filter = createTaskFilter(title, query, allowedNets, icon, "private")
        Case menu = createMenuItem(uri, id, filter, roles, [:], group, defaultHeaders)
        return [
                "filter"  : filter,
                "menuItem": menu
        ];
    }

    Case createOrUpdateCaseMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles = [:], Map<String, String> bannedRoles = [:], Case group = null, List<String> defaultHeaders = []) {
        return createOrUpdateMenuItem(id, uri, DefaultFiltersRunner.FILTER_TYPE_CASE, query, icon, title, allowedNets, roles, bannedRoles, group, defaultHeaders)
    }

    Case createOrUpdateTaskMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles = [:], Map<String, String> bannedRoles = [:],  Case group = null, List<String> defaultHeaders = []) {
        return createOrUpdateMenuItem(id, uri, DefaultFiltersRunner.FILTER_TYPE_TASK, query, icon, title, allowedNets, roles, bannedRoles, group, defaultHeaders)
    }

    Case createOrUpdateMenuItem(String id, String uri, String type, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles = [:], Map<String, String> bannedRoles = [:],  Case group = null, List<String> defaultHeaders = []) {
        Case menuItem = findMenuItem(id)
        if (!menuItem) {
            Case filter = createFilter(title, query, type, allowedNets, icon, "private", null)
            createUri(uri, UriContentType.DEFAULT)
            return createMenuItem(uri, id, filter, roles, bannedRoles)

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

}
