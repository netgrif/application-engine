package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.AsyncRunner
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.configuration.ApplicationContextProvider
import com.netgrif.workflow.importer.service.FieldFactory
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.orgstructure.domain.Member
import com.netgrif.workflow.orgstructure.service.GroupService
import com.netgrif.workflow.orgstructure.service.MemberService
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator
import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.Transition
import com.netgrif.workflow.petrinet.domain.dataset.*
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.QCase
import com.netgrif.workflow.workflow.domain.QTask
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.service.TaskService
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference
import com.querydsl.core.types.Predicate
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * ActionDelegate class contains Actions API methods.
 */
@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class ActionDelegate {

    static final Logger log = LoggerFactory.getLogger(ActionDelegate)

    static final String UNCHANGED_VALUE = "unchangedooo"
    static final String ALWAYS_GENERATE = "always"
    static final String ONCE_GENERATE = "once"

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

    @Autowired
    IPdfGenerator pdfGenerator

    /**
     * Reference of case in which current action is taking place.
     */
    Case useCase
    def map = [:]
    Action action
    FieldActionsRunner actionsRunner
    Map<String, ChangedField> changedFields = new HashMap<>()

    def init(Action action, Case useCase, FieldActionsRunner actionsRunner) {
        this.action = action
        this.useCase = useCase
        this.actionsRunner = actionsRunner
        action.fieldIds.each { name, id ->
            set(name, fieldFactory.buildFieldWithoutValidation(useCase, id))
        }
        action.transitionIds.each { name, id ->
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

    def unchanged = { return UNCHANGED_VALUE }

    /**
     * Changes behavior of a given field on given transition if certain condition is being met.
     * <br>
     * Example:
     * <pre>
     *     condition: f.conditionId,
     *     text: f.textId,
     *     transition: t.transitionId;
     *
     *     make text,visible on transition when { condition.value == true }* </pre>
     * This code will change the field <i>text</i> behaviour to <i>visible</i> when fields <i>condition</i> value is equal to <i>true</i>
     * @param field which behaviour will be changed
     * @param behavior one of visible, editable, required, optional, hidden, forbidden
     */
    def make(Field field, Closure behavior) {
        [on: { Transition trans ->
            [when: { Closure condition ->
                if (condition()) {
                    behavior(field, trans)
                    if (!changedFields.containsKey(field.stringId)) {
                        changedFields[field.stringId] = new ChangedField(field.stringId)
                    }
                    changedFields[field.stringId].addBehavior(useCase.dataSet.get(field.stringId).behavior)
                }
            }]
        }]
    }

    def saveChangedValue(Field field) {
        useCase.dataSet.get(field.stringId).value = field.value
        if (!changedFields.containsKey(field.stringId)) {
            changedFields[field.stringId] = new ChangedField(field.stringId)
        }
        changedFields[field.stringId].addAttribute("value", field.value)
    }

    def saveChangedChoices(ChoiceField field) {
        useCase.dataSet.get(field.stringId).choices = field.choices
        if (!changedFields.containsKey(field.stringId)) {
            changedFields[field.stringId] = new ChangedField(field.stringId)
        }
        changedFields[field.stringId].addAttribute("choices", field.choices.collect { it.getTranslation(LocaleContextHolder.locale) })
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

    void executeTasks(Map dataSet, String taskId, Closure<Predicate> predicateClosure) {
        List<String> caseIds = searchCases(predicateClosure)
        QTask qTask = new QTask("task")
        Page<Task> tasksPage = taskService.searchAll(qTask.transitionId.eq(taskId).and(qTask.caseId.in(caseIds)))
        tasksPage?.content?.each { task ->
            taskService.assignTask(task.stringId)
            dataService.setData(task.stringId, ImportHelper.populateDataset(dataSet as Map<String, Map<String, String>>))
            taskService.finishTask(task.stringId)
        }
    }

    void executeTask(String transitionId, Map dataSet) {
        QTask qTask = new QTask("task")
        Task task = taskService.searchOne(qTask.transitionId.eq(transitionId).and(qTask.caseId.eq(useCase.stringId)))
        taskService.assignTask(task.stringId)
        dataService.setData(task.stringId, ImportHelper.populateDataset(dataSet as Map<String, Map<String, String>>))
        taskService.finishTask(task.stringId)
    }

    List<String> searchCases(Closure<Predicate> predicates) {
        QCase qCase = new QCase("case")
        def expression = predicates(qCase)
        Page<Case> page = workflowService.searchAll(expression)

        return page.content.collect { it.stringId }
    }

    def change(Field field) {
        [about  : { cl -> // TODO: deprecated
            changeFieldValue(field, cl)
        },
         value  : { cl ->
             changeFieldValue(field, cl)
         },
         choices: { cl ->
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
         }]
    }

    void changeFieldValue(Field field, def cl) {
        def value = cl()
        if (value instanceof Closure && value() == UNCHANGED_VALUE) {
            return
        }
        if (value == null) {
            if (field instanceof FieldWithDefault && field.defaultValue != useCase.dataSet.get(field.stringId).value) {
                field.clearValue()
                saveChangedValue(field)
            } else if (!(field instanceof FieldWithDefault) && useCase.dataSet.get(field.stringId).value != null) {
                field.clearValue()
                saveChangedValue(field)
            }
            return
        }
        if (value != null) {
            field.value = value
            saveChangedValue(field)
        }
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

    Case createCase(String identifier, String title = null, String color = "", User author = userService.loggedOrSystem) {
        PetriNet net = petriNetService.getNewestVersionByIdentifier(identifier)
        if (net == null)
            throw new IllegalArgumentException("Petri net with identifier [$identifier] does not exist.")
        return createCase(net, title ?: net.defaultCaseName.defaultValue, color, author)
    }

    Case createCase(PetriNet net, String title = net.defaultCaseName.defaultValue, String color = "", User author = userService.loggedOrSystem) {
        return workflowService.createCase(net.stringId, title, color, author.transformToLoggedUser())
    }

    Task assignTask(String transitionId, Case aCase = useCase, User user = userService.loggedOrSystem) {
        String taskId = getTaskId(transitionId, aCase)
        taskService.assignTask(user.transformToLoggedUser(), taskId)
        return taskService.findOne(taskId)
    }

    Task assignTask(Task task, User user = userService.loggedOrSystem) {
        taskService.assignTask(task, user)
        return taskService.findOne(task.stringId)
    }

    void assignTasks(List<Task> tasks, User assignee = userService.loggedOrSystem) {
        taskService.assignTasks(tasks, assignee)
    }

    void cancelTask(String transitionId, Case aCase = useCase, User user = userService.loggedOrSystem) {
        String taskId = getTaskId(transitionId, aCase)
        taskService.cancelTask(user.transformToLoggedUser(), taskId)
    }

    void cancelTask(Task task, User user = userService.loggedOrSystem) {
        taskService.cancelTask(task, userService.loggedOrSystem)
    }

    void cancelTasks(List<Task> tasks, User user = userService.loggedOrSystem) {
        taskService.cancelTasks(tasks, user)
    }

    void finishTask(String transitionId, Case aCase = useCase, User user = userService.loggedOrSystem) {
        String taskId = getTaskId(transitionId, aCase)
        taskService.finishTask(user.transformToLoggedUser(), taskId)
    }

    void finishTask(Task task, User user = userService.loggedOrSystem) {
        taskService.finishTask(task, user)
    }

    void finishTasks(List<Task> tasks, User finisher = userService.loggedOrSystem) {
        taskService.finishTasks(tasks, finisher)
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

    User assignRole(String roleImportId, User user = userService.loggedUser) {
        return userService.addRole(user, roleImportId)
    }

    def setData(Task task, Map dataSet) {
        dataService.setData(task.stringId, ImportHelper.populateDataset(dataSet))
    }

    def setData(Transition transition, Map dataSet) {
        setData(transition.importId, this.useCase, dataSet)
    }

    def setData(String transitionId, Case useCase, Map dataSet) {
        def predicate = QTask.task.caseId.eq(useCase.stringId) & QTask.task.transitionId.eq(transitionId)
        def task = taskService.searchOne(predicate)
        dataService.setData(task.stringId, ImportHelper.populateDataset(dataSet))
    }

    Map<String, Field> getData(Task task) {
        def useCase = workflowService.findOne(task.caseId);
        return mapData(dataService.getData(task, useCase))
    }

    Map<String, Field> getData(Transition transition) {
        return getData(transition.stringId, this.useCase)
    }

    Map<String, Field> getData(String transitionId, Case useCase) {
        def predicate = QTask.task.caseId.eq(useCase.stringId) & QTask.task.transitionId.eq(transitionId)
        def task = taskService.searchOne(predicate)
        if (!task)
            return new HashMap<String, Field>()
        return mapData(dataService.getData(task, useCase))
    }

    protected Map<String, Field> mapData(List<Field> data) {
        return data.collectEntries {
            [(it.importId): it]
        }
    }

    Set<Group> findOrganisation(User user = loggedUser()) {
        return memberService.findByEmail(user.email)?.groups
    }

    Group createOrganisation(String name, Group parent = null, Set<User> users = [] as Set) {
        Group org = new Group(name)
        if (parent)
            org.setParentGroup(parent)
        users.collect { user ->
            org.addMember(findMember(user))
        }
        return groupService.save(org)
    }

    def deleteOrganisation(Group organisation) {
        groupService.delete(organisation)
    }

    Group saveOrganisation(Group organisation) {
        return groupService.save(organisation)
    }

    Group removeMember(Group organisation, User user) {
        organisation.members.removeAll { it.email == user.email }
        return groupService.save(organisation)
    }

    Group addMember(Group organisation, User user) {
        def member = findMember(user)
        organisation.members.add(member)
        return groupService.save(organisation)
    }

    Member findMember(User user) {
        def member = memberService.findByEmail(user.email)
        if (member == null)
            return memberService.save(Member.from(user))
        return member
    }

    User loggedUser() {
        return userService.loggedUser
    }

    File generatePDF(String transitionId){
        return pdfGenerator.convertCaseForm(useCase, transitionId)
    }

}