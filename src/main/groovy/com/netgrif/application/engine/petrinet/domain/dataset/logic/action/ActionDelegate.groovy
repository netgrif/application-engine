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
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService
import com.netgrif.application.engine.history.service.IHistoryService
import com.netgrif.application.engine.importer.service.FieldFactory
import com.netgrif.application.engine.mail.domain.MailDraft
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService
import com.netgrif.application.engine.mail.interfaces.IMailService
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
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
//import com.netgrif.application.engine.rules.domain.RuleRepository
import com.netgrif.application.engine.startup.runner.DefaultFiltersRunner
import com.netgrif.application.engine.startup.runner.FilterRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.ProcessResourceId
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
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

import java.text.Normalizer
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

//    @Autowired
//    RuleRepository ruleRepository

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
        [about      : { cl -> // TODO: deprecated
            changeFieldValue(field, cl, targetCase, targetTask)
        },
         value      : { cl ->
            changeFieldValue(field, cl, targetCase, targetTask)
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
             saveChangedChoices(field, targetCase, targetTask)
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
             saveChangedAllowedNets(field, targetCase, targetTask)
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
         validations: { cl ->
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
            if (field instanceof TaskField && targetTask.isPresent()) {
                dataService.validateTaskRefValue(value, targetTask.get().getStringId());
            }
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

    Case findCase(Closure<Predicate> predicate) {
        QCase qCase = new QCase("case")
        return workflowService.searchOne(predicate(qCase))
    }

    Case createCase(String identifier, String title = null, String color = "", IUser author = userService.loggedOrSystem, Locale locale = LocaleContextHolder.getLocale(), Map<String, String> params = [:]) {
        return workflowService.createCaseByIdentifier(identifier, title, color, author.transformToLoggedUser(), locale, params).getCase()
    }

    Case createCase(PetriNet net, String title = net.defaultCaseName.getTranslation(locale), String color = "", IUser author = userService.loggedOrSystem, Locale locale = LocaleContextHolder.getLocale(), Map<String, String> params = [:]) {
        CreateCaseEventOutcome outcome = workflowService.createCase(net.stringId, title, color, author.transformToLoggedUser(), params)
        this.outcomes.add(outcome)
        return outcome.getCase()
    }

    Task assignTask(String transitionId, Case aCase = useCase, IUser user = userService.loggedOrSystem, Map<String, String> params = [:]) {
        String taskId = getTaskId(transitionId, aCase)
        AssignTaskEventOutcome outcome = taskService.assignTask(user.transformToLoggedUser(), taskId, params)
        this.outcomes.add(outcome)
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
        def predicate = QTask.task.caseId.eq(useCase.stringId) & QTask.task.transitionId.eq(transitionId)
        def task = taskService.searchOne(predicate)
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
     * Action API case search function using Elasticsearch database
     * @param requests the CaseSearchRequest list
     * @param loggedUser the user who is searching for the requests
     * @param page the order of page to return. by default it returns the first page
     * @param pageable the page configuration that will contain the requests
     * @param locale the Locale to be used when searching for requests
     * @param isIntersection to decide null query handling
     * @return page of cases
     * */
    Page<Task> findTasks(List<ElasticTaskSearchRequest> requests, LoggedUser loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
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
    Page<Task> findTasks(Map<String, Object> request, LoggedUser loggedUser = userService.loggedOrSystem.transformToLoggedUser(),
                         int page = 1, int pageSize = 25, Locale locale = Locale.default, boolean isIntersection = false) {
        List<ElasticTaskSearchRequest> requests = Collections.singletonList(new ElasticTaskSearchRequest(request))
        return findTasks(requests, loggedUser, page, pageSize, locale, isIntersection)
    }

    List<Case> findDefaultFilters() {
        if (!createDefaultFilters) {
            return []
        }
        return findCases({ it.processIdentifier.eq(FilterRunner.FILTER_PETRI_NET_IDENTIFIER).and(it.author.id.eq(userService.system.stringId)) })
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
        filterCase.dataSet[DefaultFiltersRunner.FILTER_I18N_TITLE_FIELD_ID].value = (title instanceof I18nString) ? title : new I18nString(title as String)
        filterCase = workflowService.save(filterCase)
        Task newFilterTask = taskService.findOne(filterCase.tasks.find { it.transition == DefaultFiltersRunner.AUTO_CREATE_TRANSITION }.task)
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
                        "filterMetadata": filterMetadata ?: defaultFilterMetadata(type)
                ]
        ]
        setData(newFilterTask, setDataMap)
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
     * create menu item for given filter instance
     * @param uri
     * @param identifier - unique item identifier
     * @param filter
     * @param groupName
     * @param allowedRoles ["role_import_id": "net_import_id"]
     * @param bannedRoles ["role_import_id": "net_import_id"]
     * @return
     */
    @Deprecated
    Case createMenuItem(String uri, String identifier, Case filter, String groupName, Map<String, String> allowedRoles, Map<String, String> bannedRoles = [:], List<String> caseDefaultHeaders = [], List<String> taskDefaultHeaders = []) {
        MenuItemBody body = new MenuItemBody(
                uri,
                identifier,
                filter.dataSet[FILTER_FIELD_I18N_FILTER_NAME].value as I18nString,
                null
        )
        body.setFilter(filter)
        body.setCaseDefaultHeaders(caseDefaultHeaders)
        body.setTaskDefaultHeaders(taskDefaultHeaders)
        body.setAllowedRoles(collectRolesForPreferenceItem(allowedRoles))
        body.setBannedRoles(collectRolesForPreferenceItem(bannedRoles))
        body.setUseCustomView(false)
        body.setCaseRequireTitleInCreation(true)

        return createMenuItem(body)
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
    @Deprecated
    Case createMenuItem(String uri, String identifier, Case filter, String groupName, List<ProcessRole> allowedRoles, List<ProcessRole> bannedRoles = [], List<String> caseDefaultHeaders = [], List<String> taskDefaultHeaders = []) {
        MenuItemBody body = new MenuItemBody(
                uri,
                identifier,
                filter.dataSet[FILTER_FIELD_I18N_FILTER_NAME].value as I18nString,
                null
        )
        body.setFilter(filter)
        body.setCaseDefaultHeaders(caseDefaultHeaders)
        body.setTaskDefaultHeaders(taskDefaultHeaders)
        body.setAllowedRoles(collectRolesForPreferenceItem(allowedRoles))
        body.setBannedRoles(collectRolesForPreferenceItem(bannedRoles))
        body.setUseCustomView(false)
        body.setCaseRequireTitleInCreation(true)

        return createMenuItem(body)
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
     * @return
     */
    @Deprecated
    Case createMenuItem(String uri, String identifier, Case filter, Map<String, String> allowedRoles, Map<String, String> bannedRoles = [:], Case group = null, List<String> caseDefaultHeaders = [], List<String> taskDefaultHeaders = []) {
        MenuItemBody body = new MenuItemBody(
                uri,
                identifier,
                filter.dataSet[FILTER_FIELD_I18N_FILTER_NAME].value as I18nString,
                null
        )
        body.setFilter(filter)
        body.setCaseDefaultHeaders(caseDefaultHeaders)
        body.setTaskDefaultHeaders(taskDefaultHeaders)
        body.setAllowedRoles(collectRolesForPreferenceItem(allowedRoles))
        body.setBannedRoles(collectRolesForPreferenceItem(bannedRoles))
        body.setUseCustomView(false)
        body.setCaseRequireTitleInCreation(true)

        return createMenuItem(body)
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
    @Deprecated
    Case createMenuItem(String uri, String identifier, Case filter, List<ProcessRole> allowedRoles, List<ProcessRole> bannedRoles = [], Case group = null, List<String> caseDefaultHeaders = [], List<String> taskDefaultHeaders = []) {
        MenuItemBody body = new MenuItemBody(
                uri,
                identifier,
                filter.dataSet[FILTER_FIELD_I18N_FILTER_NAME].value as I18nString,
                null
        )
        body.setFilter(filter)
        body.setCaseDefaultHeaders(caseDefaultHeaders)
        body.setTaskDefaultHeaders(taskDefaultHeaders)
        body.setAllowedRoles(collectRolesForPreferenceItem(allowedRoles))
        body.setBannedRoles(collectRolesForPreferenceItem(bannedRoles))
        body.setUseCustomView(false)
        body.setCaseRequireTitleInCreation(true)

        return createMenuItem(body)
    }

    /**
     * Creates item in menu with given parameters
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
     * @return created Case of preference_item
     * */
    @NamedVariant
    Case createMenuItem(String uri, String identifier, def name, String icon = "filter_none", Case filter = null,
                        Map<String, String> allowedRoles = [:], Map<String, String> bannedRoles = [:],
                        List<String> caseDefaultHeaders = [], List<String> taskDefaultHeaders = []) {
        MenuItemBody body = new MenuItemBody(
                uri,
                identifier,
                (name instanceof I18nString) ? name : new I18nString(name as String),
                icon
        )
        body.setFilter(filter)
        body.setCaseDefaultHeaders(caseDefaultHeaders)
        body.setTaskDefaultHeaders(taskDefaultHeaders)
        body.setAllowedRoles(collectRolesForPreferenceItem(allowedRoles))
        body.setBannedRoles(collectRolesForPreferenceItem(bannedRoles))
        body.setUseCustomView(false)
        body.setCaseRequireTitleInCreation(true)

        return createMenuItem(body)
    }

    /**
     * Changes data of provided preference_item instance. These attributes can be changed:
     * <ul>
     * <li> <code>changeMenuItem item allowedRoles { ["role_1":"my_process_id"] }</code>
     * <li> <code>changeMenuItem item bannedRoles { ["role_1":"my_process_id"] }</code>
     * <li> <code>changeMenuItem item caseDefaultHeaders { ["meta-title","meta-visualId"] }</code>
     * <li> <code>changeMenuItem item taskDefaultHeaders { ["meta-title","meta-caseId"] }</code>
     * <li> <code>changeMenuItem item filter { filterCase }</code>
     * <li> <code>changeMenuItem item uri { "/my_node1/my_node2" }</code>
     * <li> <code>changeMenuItem item title { new I18nString("New title") }</code>
     * <li> <code>changeMenuItem item title { "New title" }</code>
     * <li> <code>changeMenuItem item menuIcon { "filter_alt" }</code>
     * <li> <code>changeMenuItem item tabIcon { "filter_none" }</code>
     * </ul>
     * @param item {@link Case} instance of preference_item.xml
     */
    def changeMenuItem(Case item) {
        [allowedRoles  : { cl ->
            updateMenuItemRoles(item, cl as Closure, MenuItemConstants.PREFERENCE_ITEM_FIELD_ALLOWED_ROLES.attributeId)
        },
         bannedRoles   : { cl ->
             updateMenuItemRoles(item, cl as Closure, MenuItemConstants.PREFERENCE_ITEM_FIELD_BANNED_ROLES.attributeId)
         },
         caseDefaultHeaders: { cl ->
             String defaultHeaders = cl() as String
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS.attributeId): ["type": "text", "value": defaultHeaders]
             ])
         },
         taskDefaultHeaders: { cl ->
             String defaultHeaders = cl() as String
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS.attributeId): ["type": "text", "value": defaultHeaders]
             ])
         },
         filter        : { cl ->
             def filter = cl() as Case
             setData("change_filter", item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_NEW_FILTER_ID.attributeId): ["type": "text", "value": filter.stringId]
             ])
         },
         uri           : { cl ->
             def uri = cl() as String
             def aCase = useCase
             if (useCase == null || item.stringId != useCase.stringId) {
                 aCase = workflowService.findOne(item.stringId)
             }
             moveMenuItem(aCase, uri)
         },
         title         : { cl ->
             def value = cl()
             I18nString newName = (value instanceof I18nString) ? value : new I18nString(value as String)
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_NAME.attributeId): ["type": "i18n", "value": newName]
             ])
         },
         menuIcon         : { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_ICON.attributeId): ["type": "text", "value": value]
             ])
         },
         tabIcon         : { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_TAB_ICON.attributeId): ["type": "text", "value": value]
             ])
         },
         requireTitleInCreation: { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_REQUIRE_TITLE_IN_CREATION.attributeId): ["type": "boolean", "value": value]
             ])
         },
         useCustomView: { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_CUSTOM_VIEW.attributeId): ["type": "boolean", "value": value]
             ])
         },
         customViewSelector: { cl ->
             def value = cl()
             setData(MenuItemConstants.PREFERENCE_ITEM_SETTINGS_TRANS_ID.attributeId, item, [
                     (MenuItemConstants.PREFERENCE_ITEM_FIELD_CUSTOM_VIEW_SELECTOR.attributeId): ["type": "text", "value": value]
             ])
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
     * @param groupName - name of group to add menu item to
     * @param allowedRoles
     * @param bannedRoles
     * @param visibility - "private" or "public"
     * @return
     */
    @Deprecated
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
    @Deprecated
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

    /**
     * Creates filter and preference_item instances with given parameters.
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
     * @return created {@link Case} instance of preference_item
     * */
    @NamedVariant
    Case createFilterInMenu(String uri, String itemIdentifier, def itemAndFilterName, String filterQuery,
                            String filterType, String filterVisibility, List<String> filterAllowedNets = [],
                            String itemAndFilterIcon = "filter_none", Map<String, String> itemAllowedRoles = [:],
                            Map<String, String> itemBannedRoles = [:], List<String> itemCaseDefaultHeaders = [],
                            List<String> itemTaskDefaultHeaders = [], def filterMetadata = null) {
        Case filter = createFilter(itemAndFilterName, filterQuery, filterType, filterAllowedNets, itemAndFilterIcon, filterVisibility, filterMetadata)
        Case menuItem = createMenuItem(uri, itemIdentifier, itemAndFilterName, itemAndFilterIcon, filter, itemAllowedRoles, itemBannedRoles, itemCaseDefaultHeaders, itemTaskDefaultHeaders)
        return menuItem
    }

    /**
     * Creates filter and preference_item instances with given parameters.
     *
     * @param body configuration class for menu item creation
     * @param filterQuery elastic query for filter
     * @param filterType type of filter. Possible values: {@value DefaultFiltersRunner#FILTER_TYPE_CASE} or
     * {@value DefaultFiltersRunner#FILTER_TYPE_TASK}
     * @param filterVisibility possible values: {@value DefaultFiltersRunner#FILTER_VISIBILITY_PRIVATE} or
     * {@value DefaultFiltersRunner#FILTER_VISIBILITY_PUBLIC}
     * @param filterAllowedNets List of allowed nets. Element of list is process identifier
     * @param filterMetadata metadata for filter. If no value is provided, then default value is used: {@link #defaultFilterMetadata(String)}
     *
     * @return created {@link Case} instance of preference_item
     * */
    Case createFilterInMenu(MenuItemBody body, String filterQuery, String filterType, String filterVisibility,
                            List<String> filterAllowedNets = [], def filterMetadata = null) {
        Case filter = createFilter(body.menuName, filterQuery, filterType, filterAllowedNets, body.menuIcon, filterVisibility, filterMetadata)
        body.filter = filter
        Case menuItem = createMenuItem(body)
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
        Task newItemTask = taskService.findOne(menuItemCase.tasks.find { it.transition == MenuItemConstants.PREFERENCE_ITEM_FIELD_INIT_TRANS_ID.attributeId }.task)
        String nodePath = createNodePath(body.uri, sanitizedIdentifier)
        uriService.getOrCreate(nodePath, UriContentType.CASE)

        newItemTask = assignTask(newItemTask)
        setData(newItemTask, body.toDataSet(parentItemCase.stringId, nodePath))
        finishTask(newItemTask)

        return workflowService.findOne(menuItemCase.stringId)
    }

    protected String sanitize(String input) {
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[\\W-]+", "-")
                .toLowerCase()
    }

    protected String createNodePath(String uri, String identifier) {
        if (uri == uriService.getUriSeparator()) {
            return uri + identifier
        } else {
            return uri + uriService.getUriSeparator() + identifier
        }
    }

    protected Case getOrCreateFolderItem(String uri) {
        UriNode node = uriService.getOrCreate(uri, UriContentType.CASE)
        MenuItemBody body = new MenuItemBody(new I18nString(node.name),"folder")
        return getOrCreateFolderRecursive(node, body)
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
        Task newItemTask = taskService.findOne(folder.tasks.find { it.transition == MenuItemConstants.PREFERENCE_ITEM_FIELD_INIT_TRANS_ID.attributeId }.task)
        assignTask(newItemTask)
        setData(newItemTask, body.toDataSet(null, node.path))
        finishTask(newItemTask)

        folder = workflowService.findOne(folder.stringId)
        if (node.parentId != null) {
            UriNode parentNode = uriService.findById(node.parentId)
            body = new MenuItemBody(new I18nString(parentNode.name), "folder")

            getOrCreateFolderRecursive(parentNode, body, folder)
        }

        return folder
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

        List<String> parentIdList = item.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId].value as ArrayList<String>
        if (parentIdList != null && parentIdList.size() > 0) {
            Case oldParent = removeChildItemFromParent(parentIdList[0], item)
            casesToSave.add(oldParent)
        }

        UriNode destNode = uriService.getOrCreate(destUri, UriContentType.CASE)
        Case newParent = getOrCreateFolderItem(destNode.path)
        if (newParent != null) {
            item.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId].value = [newParent.stringId] as ArrayList
            newParent = appendChildCaseId(newParent, item.stringId)
            casesToSave.add(newParent)
        } else {
            item.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId].value = null
        }

        item.uriNodeId = destNode.stringId
        item = resolveAndHandleNewNodePath(item, destNode.path)
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

    /**
     * Duplicates menu item. It creates new preference_item instance with the same {@link Case#dataSet} as the provided
     * item instance. The only difference is in title, menu_item_identifier and associations
     *
     * @param originItem Menu item instance, which is duplicated
     * @param newTitle Title of menu item, that is displayed in menu and tab. Cannot be empty or null.
     * @param newIdentifier unique menu item identifier
     *
     * @return duplicated {@link Case} instance of preference_item
     * */
    Case duplicateMenuItem(Case originItem, I18nString newTitle, String newIdentifier) {
        if (!newIdentifier) {
            throw new IllegalArgumentException("View item identifier is null!")
        }
        if (newTitle == null || newTitle.defaultValue == "") {
            throw new IllegalArgumentException("Default title is empty")
        }
        String sanitizedIdentifier = sanitize(newIdentifier)
        if (existsMenuItem(sanitizedIdentifier)) {
            throw new IllegalArgumentException("View item identifier $sanitizedIdentifier is not unique!")
        }

        Case duplicated = createCase(FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER, newTitle.defaultValue)
        duplicated.uriNodeId = originItem.uriNodeId
        duplicated.dataSet = originItem.dataSet
        duplicated.title = newTitle.defaultValue
        duplicated = workflowService.save(duplicated)

        UriNode node = uriService.findById(originItem.uriNodeId)
        String newNodePath = createNodePath(node.path, sanitizedIdentifier)
        uriService.getOrCreate(newNodePath, UriContentType.CASE)

        Task newItemTask = taskService.findOne(duplicated.tasks.find { it.transition == MenuItemConstants.PREFERENCE_ITEM_FIELD_INIT_TRANS_ID.attributeId }.task)
        Map updatedDataSet = [
                (MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE.attributeId): [
                        "value": null,
                        "type": "text"
                ],
                (MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER.attributeId): [
                        "value": null,
                        "type": "text"
                ],
                (MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_NAME.attributeId): [
                        "value": newTitle,
                        "type": "i18n"
                ],
                (MenuItemConstants.PREFERENCE_ITEM_FIELD_TAB_NAME.attributeId): [
                        "value": newTitle,
                        "type": "i18n"
                ],
                (MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId): [
                        "value": newNodePath,
                        "type": "text"
                ],
                // Must be reset by button, because we have the same dataSet reference between originItem and duplicated
                (MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_RESET_CHILD_ITEM_IDS.attributeId): [
                        "value": 0,
                        "type": "button"
                ],
        ]
        assignTask(newItemTask)
        dataService.setData(newItemTask, ImportHelper.populateDataset(updatedDataSet))
        finishTask(newItemTask)

        String parentId = (originItem.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId].value as ArrayList).get(0)
        if (parentId) {
            Case parent = workflowService.findOne(parentId)
            appendChildCaseIdAndSave(parent, duplicated.stringId)
        }
        return workflowService.findOne(duplicated.stringId)
    }

    private List<Case> updateNodeInChildrenFoldersRecursive(Case parentFolder) {
        List<String> childItemIds = parentFolder.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId].value as List<String>
        if (childItemIds == null || childItemIds.isEmpty()) {
            return new ArrayList<Case>()
        }

        List<Case> children = workflowService.findAllById(childItemIds)

        List<Case> casesToSave = new ArrayList<>()
        for (child in children) {
            UriNode parentNode = uriService.getOrCreate(parentFolder.getFieldValue(MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId) as String, UriContentType.CASE)
            child.uriNodeId = parentNode.stringId
            child = resolveAndHandleNewNodePath(child, parentNode.path)

            casesToSave.add(child)
            casesToSave.addAll(updateNodeInChildrenFoldersRecursive(child))
        }

        return casesToSave
    }

    private Case resolveAndHandleNewNodePath(Case folderItem, String destUri) {
        String newNodePath = resolveNewNodePath(folderItem, destUri)
        UriNode newNode = uriService.getOrCreate(newNodePath, UriContentType.CASE)
        folderItem.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId].value = newNode.path

        return folderItem
    }

    private String resolveNewNodePath(Case folderItem, String destUri) {
        return destUri +
                uriService.getUriSeparator() +
                folderItem.getFieldValue(MenuItemConstants.PREFERENCE_ITEM_FIELD_IDENTIFIER.attributeId) as String
    }

    private Case removeChildItemFromParent(String folderId, Case childItem) {
        Case parentFolder = workflowService.findOne(folderId)
        (parentFolder.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId].value as List).remove(childItem.stringId)
        parentFolder.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_HAS_CHILDREN.attributeId].value = hasChildren(parentFolder)
        workflowService.save(parentFolder)
    }

    private boolean isCyclicNodePath(Case folderItem, String destUri) {
        String oldNodePath = folderItem.getFieldValue(MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId)
        return destUri.contains(oldNodePath)
    }

    private boolean hasChildren(Case folderItem) {
        List children = folderItem.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId].value as List
        return children != null && children.size() > 0
    }

    private Case appendChildCaseIdAndSave(Case folderCase, String childItemCaseId) {
        folderCase = appendChildCaseId(folderCase, childItemCaseId)
        return workflowService.save(folderCase)
    }

    private Case appendChildCaseId(Case folderCase, String childItemCaseId) {
        List<String> childIds = folderCase.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId].value as ArrayList<String>
        if (childIds == null) {
            folderCase.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId].value = [childItemCaseId] as ArrayList
        } else {
            folderCase.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId].value = childIds + [childItemCaseId] as ArrayList
        }

        folderCase.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_HAS_CHILDREN.attributeId].value = hasChildren(folderCase)

        return folderCase
    }

    private Case initializeParentId(Case childFolderCase, String parentFolderCaseId) {
        childFolderCase.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId].value = [parentFolderCaseId] as ArrayList
        return workflowService.save(childFolderCase)
    }

    protected Case findFolderCase(UriNode node) {
        return findCaseElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.nodePath.textValue.keyword:\"$node.path\"")
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
        String filterId = (item.dataSet[MenuItemConstants.PREFERENCE_ITEM_FIELD_FILTER_CASE.attributeId].value as List)[0] as String
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

    @Deprecated
    Map<String, Case> createMenuItem(String id, String uri, String query, String icon, String title, List<String> allowedNets, Map<String, String> roles, Map<String, String> bannedRoles = [:], Case group = null, List<String> defaultHeaders = []) {
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
    Case createOrUpdateMenuItem(String uri, String identifier, def name, String icon = "filter_none", Case filter = null,
                                Map<String, String> allowedRoles = [:], Map<String, String> bannedRoles = [:],
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
    Case createOrUpdateMenuItemAndFilter(String uri, String itemIdentifier, def itemAndFilterName, String filterQuery,
                                         String filterType, String filterVisibility, List<String> filterAllowedNets = [],
                                         String itemAndFilterIcon = "filter_none", Map<String, String> itemAllowedRoles = [:],
                                         Map<String, String> itemBannedRoles = [:], List<String> itemCaseDefaultHeaders = [],
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
        if (item) {
            return updateMenuItem(item, body)
        } else {
            return createMenuItem(body)
        }
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
        List<String> splitPathList = splitUriPath(node.path)

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
        return findOptionsBasedOnSelectedNode(node, splitUriPath(node.path))
    }

    Map<String, I18nString> findOptionsBasedOnSelectedNode(UriNode node, List<String> splitPathList) {
        Map<String, I18nString> options = new HashMap<>()

        options.putAll(splitPathList.collectEntries { [(it): new I18nString(it)]})

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

        return node.path
    }

    Field<?> getFieldOfTask(String taskId, String fieldId) {
        Task task = taskService.findOne(taskId)
        Case taskCase = workflowService.findOne(task.caseId)
        return taskCase.getPetriNet().getDataSet().get(fieldId)
    }
}
