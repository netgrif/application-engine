package com.netgrif.application.engine.action

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest
import com.netgrif.application.engine.configuration.PublicViewProperties
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.domain.version.Version
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.DataField
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import javax.mail.internet.MimeMessage

import static java.util.Base64.getEncoder
import static org.junit.jupiter.api.Assertions.*

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ActionDelegateTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ActionDelegate actionDelegate

    @Autowired
    private IUserService userService

    @Autowired
    private PublicViewProperties publicViewProperties

    @Autowired
    private IPetriNetService petriNetService

    private static final ACTION_API_NET_IDENTIFIER = "action_api_improvements"
    private static final ERROR_MESSAGE_TEMPLATE = "field [|fieldId|] in [|testedMethod|] method returned null"
    private static final FIELD_ID_TEMPLATE = "|fieldId|"
    private static final TESTED_METHOD_TEMPLATE = "|testedMethod|"

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        actionDelegate.outcomes = []
    }

    @Test
    void inviteUser() {
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"))
        smtpServer.start()

        MessageResource messageResource = actionDelegate.inviteUser("test@netgrif.com")
        assert messageResource.getContent().success

        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assert messages
        smtpServer.stop()
    }

    @Test
    void deleteUser() {
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"))
        smtpServer.start()
        String mail = "test@netgrif.com";
        MessageResource messageResource = actionDelegate.inviteUser(mail)
        assert messageResource.getContent().success
        IUser user = userService.findByEmail(mail, false)
        assert user != null
        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assert messages
        actionDelegate.deleteUser(mail)
        IUser user2 = userService.findByEmail(mail, false)
        assert user2 == null
        smtpServer.stop()
    }


    @Test
    void inviteUserNewUserRequest() {
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"))
        smtpServer.start()

        NewUserRequest newUserRequest = new NewUserRequest()
        newUserRequest.setEmail("test@netgrif.com")
        newUserRequest.groups = new HashSet<>()
        newUserRequest.processRoles = new HashSet<>()

        MessageResource messageResource = actionDelegate.inviteUser(newUserRequest)
        assert messageResource.getContent().success

        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assert messages
        smtpServer.stop()
    }

    @Test
    void makeUrlAction() {
        final String identifier = "identifier"
        final String url = "test.public.url/${getEncoder().encodeToString(identifier.bytes)}"
        assert actionDelegate.makeUrl(identifier) == url
        assert actionDelegate.makeUrl(publicViewProperties.url, identifier) == url
        assert actionDelegate.makeUrl("test.netgrif.com/public", "identifier") == "test.netgrif.com/public/${getEncoder().encodeToString(identifier.bytes)}"
    }

    @Test
    void testTaskActions() {
        importTestPetriNet()
        Case testCase = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)
        List<String> taskIds = testCase.tasks.collect { it.task }
        List<Task> tasks = actionDelegate.findTasks(taskIds)

        assertNotNull(tasks)
        assertFalse(tasks.empty)
        assertEquals(tasks.size(), taskIds.size())

        testCase = actionDelegate.setData("t1", testCase, [
                "enumeration_map" : [
                        "type" : "enumeration_map",
                        "value": taskIds[0]
                ],
                "multichoice_map" : [
                        "type" : "multichoice_map",
                        "value": taskIds
                ],
                "stringCollection": [
                        "type" : "stringCollection",
                        "value": taskIds
                ],
                "taskRef"         : [
                        "type" : "taskRef",
                        "value": taskIds
                ],
                "text"            : [
                        "type" : "text",
                        "value": taskIds[0]
                ]
        ]).case
        actionDelegate.useCase = testCase

//        fields with single value
        ["enumeration_map", "text"].forEach {
            assertTaskSearchResults(it, testCase, 1, true)
        }

//        fields with collection value
        ["multichoice_map", "stringCollection", "taskRef"].forEach {
            assertTaskSearchResults(it, testCase, taskIds.size(), false)
        }
    }

    @Test
    void testCaseActions() {
        importTestPetriNet()
        Case testCase1 = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)
        Case testCase2 = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)
        Case testCase3 = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)
        Case testCase4 = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)

        List<String> caseIds = [testCase1.stringId, testCase2.stringId, testCase3.stringId, testCase4.stringId]

        Case searchedCase = actionDelegate.findCase(caseIds[0])
        assertNotNull(searchedCase)
        assertEquals(searchedCase.stringId, testCase1.stringId)

        List<Case> searchedCases = actionDelegate.findCases(caseIds)
        assertNotNull(searchedCases)
        assertFalse(searchedCases.empty)
        assertEquals(searchedCases.size(), caseIds.size())

        testCase1 = actionDelegate.setData("t1", testCase1, [
                "enumeration_map" : [
                        "type" : "enumeration_map",
                        "value": caseIds[0]
                ],
                "multichoice_map" : [
                        "type" : "multichoice_map",
                        "value": caseIds
                ],
                "stringCollection": [
                        "type" : "stringCollection",
                        "value": caseIds
                ],
                "caseRef"         : [
                        "type" : "caseRef",
                        "value": caseIds
                ],
                "text"            : [
                        "type" : "text",
                        "value": caseIds[0]
                ]
        ]).case

        actionDelegate.useCase = testCase1
//        fields with single value
        ["enumeration_map", "text"].forEach {
            assertCaseSearchResults(it, testCase1, 1, true)
        }

//        fields with collection value
        ["multichoice_map", "stringCollection", "caseRef"].forEach {
            assertCaseSearchResults(it, testCase1, caseIds.size(), false)
        }
    }

    @Test
    void testCaseDeletionActions() {
        importTestPetriNet()
        Case testCase1 = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)
        Case testCase2 = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)

        testCase1 = actionDelegate.deleteCase(testCase1)
        assertCaseDeletion(testCase1.stringId)

        testCase2 = actionDelegate.deleteCase(testCase2.stringId)
        assertCaseDeletion(testCase2.stringId)
    }

    @Test
    void testPetriNetActions() {
        PetriNet importedNet = importTestPetriNet()
        String mongoId = importedNet.stringId
        String netIdentifier = importedNet.getIdentifier()
        ObjectId objectId = importedNet.objectId

        PetriNet foundNet = actionDelegate.findPetriNet(mongoId)
        assertNotNull(foundNet)
        assertEquals(foundNet.objectId, objectId)
        assertEquals(foundNet.identifier, netIdentifier)
        foundNet = null

        foundNet = actionDelegate.findPetriNet(objectId)
        assertNotNull(foundNet)
        assertEquals(foundNet.stringId, mongoId)
        assertEquals(foundNet.identifier, netIdentifier)
        foundNet = null

        PetriNet menuItemNet = petriNetService.getByIdentifier("menu_item")[0]
        String mongoId2 = menuItemNet.stringId
        ObjectId objectId3 = menuItemNet.objectId

        def searchTargets = [mongoId, mongoId2]
        List<PetriNet> searchResults = actionDelegate.findPetriNets(searchTargets as List<String>)
        assertNotNull(searchResults)
        assertFalse(searchResults.empty)
        assertTrue(searchResults.collect { it.stringId }.containsAll(searchTargets))
        searchResults = null

        searchTargets = [objectId, objectId3]
        searchResults = actionDelegate.findPetriNetsByObjectIds(searchTargets as List<ObjectId>)
        assertNotNull(searchResults)
        assertFalse(searchResults.empty)
        assertTrue(searchResults.collect { it.objectId }.containsAll(searchTargets))

        PetriNet importedNet2 = importTestPetriNet()
        assertNotNull(importedNet2)
        assertEquals(importedNet2.identifier, netIdentifier)
        assertNotEquals(importedNet2.stringId, mongoId)
        assertNotEquals(importedNet2.version, importedNet.version)

        foundNet = actionDelegate.findPetriNetByIdentifier(importedNet2.identifier, new Version(1, 0, 0))
        assertNotNull(foundNet, null)
        assertEquals(foundNet.identifier, netIdentifier)
        assertEquals(foundNet.stringId, mongoId)
        assertEquals(foundNet.version, importedNet.version)
        assertNotEquals(foundNet.version, importedNet2.version)
        foundNet = null

//        find newest
        foundNet = actionDelegate.findPetriNetByIdentifier(importedNet2.identifier)
        assertNotNull(foundNet)
        assertEquals(foundNet.identifier, netIdentifier)
        assertNotEquals(foundNet.stringId, mongoId)
        assertNotEquals(foundNet.version, importedNet.version)
        assertEquals(foundNet.version, importedNet2.version)
    }

    @Test
    void testOptionsActions() {
        PetriNet net = importTestPetriNet()
        Case case1 = actionDelegate.createCase(net, "Test title 1")
        Case case2 = actionDelegate.createCase(net, "Test title 2")

        List<Case> cases = [case1, case2]
        Map<String, I18nString> options = actionDelegate.casesToOptions(cases)
        assertNotNull(options)
        assertEquals(options.size(), cases.size())
        assertTrue(options.keySet().containsAll(cases.collect { it.stringId }))
        assertEquals(options.get(case1.stringId).defaultValue, case1.title)
        assertEquals(options.get(case2.stringId).defaultValue, case2.title)
        options = null

        String keyTransformationTestString = "Key transformation test "
        String valueTransformationTestString = "Value transformation test "
        options = actionDelegate.casesToOptions(cases, { return "Value transformation test ".concat(it.title) }, { return "Key transformation test ".concat(it.stringId) })
        assertNotNull(options)
        assertEquals(options.size(), cases.size())
        assertTrue(options.keySet().containsAll(cases.collect { keyTransformationTestString.concat(it.stringId) }))
        assertEquals(options.get(keyTransformationTestString.concat(case1.stringId)).defaultValue, valueTransformationTestString.concat(case1.title))
        assertEquals(options.get(keyTransformationTestString.concat(case2.stringId)).defaultValue, valueTransformationTestString.concat(case2.title))

        case1.dataSet["enumeration_map"].options = options
        actionDelegate.useCase = case1
        actionDelegate.initFieldsMap(["enumeration_map": "enumeration_map"])
        EnumerationMapField field = (EnumerationMapField) actionDelegate.map.get("enumeration_map")
        assertNotNull(field)
        assertNotNull(field.options)
        I18nString option = field.getOption(keyTransformationTestString.concat(case1.stringId))
        assertNotNull(option)
        assertEquals(option.defaultValue, valueTransformationTestString.concat(case1.title))
    }

    @Test
    void testTaskEventActions() {
        importTestPetriNet()
        Case testCase = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)
        List<String> transitionIds = ["t1", "t2"]
        IUser user = userService.getLoggedOrSystem()

//        testing "byTransition" variants should be enough, as they call methods, that take List<Task> instead of List<String>
        List<Task> tasks = actionDelegate.assignTasksByTransitions(transitionIds, testCase)
        assertNotNull(tasks)
        assertEquals(tasks.size(), transitionIds.size())
        assertTrue(tasks.stream().allMatch { it.user.email == user.email })
        assertTrue(tasks.stream().allMatch { it.userId == user.stringId })
        assertTrue(tasks.stream().allMatch { it.startDate != null })
        tasks = null

        tasks = actionDelegate.cancelTasksByTransitions(transitionIds, testCase)
        assertNotNull(tasks)
        assertEquals(tasks.size(), transitionIds.size())
        assertTrue(tasks.stream().allMatch { it.userId == null })
        assertTrue(tasks.stream().allMatch { it.startDate == null })
        tasks = null

        actionDelegate.assignTasksByTransitions(transitionIds, testCase)
        tasks = actionDelegate.finishTasksByTransitions(transitionIds, testCase)
        assertNotNull(tasks)
        assertEquals(tasks.size(), transitionIds.size())
        assertTrue(tasks.stream().allMatch { it.finishedBy == user.stringId })
        assertTrue(tasks.stream().allMatch { it.finishDate != null })
        assertTrue(tasks.stream().allMatch { it.userId == null })
        tasks = actionDelegate.findTasks(tasks.collect { it.stringId })
        assertEquals(tasks.size(), 1)
        assertEquals(tasks[0].transitionId, "t2")
    }

    private void assertCaseDeletion(String deletedCaseId) {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            actionDelegate.findCase(deletedCaseId)
        })

        String expectedMessage = "Could not find Case with id [${deletedCaseId}]"
        assertEquals(expectedMessage, e.getMessage())
    }

    private void assertTaskSearchResults(String fieldId, Case testCase, int sizeToCheck, boolean singleValueField) {
        actionDelegate.initFieldsMap([(fieldId): fieldId])
        String errorMessageWithFieldId = ERROR_MESSAGE_TEMPLATE.replace(FIELD_ID_TEMPLATE, fieldId)
        Field field = actionDelegate.map.get(fieldId)
        String firstTaskId = ([field.value].flatten() as List<String>)[0]

        Task task = actionDelegate.findTask(field)
        String errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findTask(Field)")
        assertNotNull(task, errorMessage)
        assertEquals(task.stringId, firstTaskId, errorMessage)
        task = null

        List<Task> tasks = actionDelegate.findTasks(field)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findTasks(Field)")
        assertNotNull(tasks, errorMessage)
        assertFalse(tasks.empty, errorMessage)
        assertEquals(tasks.size(), sizeToCheck, errorMessage)
        if (singleValueField) {
            assertEquals(tasks[0].stringId, firstTaskId, errorMessage)
        }
        tasks = null

        DataField dataField = testCase.getDataField(fieldId)
        task = actionDelegate.findTask(dataField)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findTask(DataField)")
        assertNotNull(task, errorMessage)
        assertEquals(task.stringId, firstTaskId, errorMessage)

        tasks = actionDelegate.findTasks(dataField)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findTasks(DataField)")
        assertNotNull(tasks, errorMessage)
        assertFalse(tasks.empty, errorMessage)
        assertEquals(tasks.size(), sizeToCheck, errorMessage)
        if (singleValueField) {
            assertEquals(tasks[0].stringId, firstTaskId, errorMessage)
        }
    }

    private void assertCaseSearchResults(String fieldId, Case testCase, int sizeToCheck, boolean singleValueField) {
        actionDelegate.initFieldsMap([(fieldId): fieldId])
        String errorMessageWithFieldId = ERROR_MESSAGE_TEMPLATE.replace(FIELD_ID_TEMPLATE, fieldId)
        Field field = actionDelegate.map.get(fieldId)
        String firstCaseId = ([field.value].flatten() as List<String>)[0]

        Case searchedCase = actionDelegate.findCase(field)
        String errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findCase(Field)")
        assertNotNull(searchedCase, errorMessage)
        assertEquals(searchedCase.stringId, firstCaseId, errorMessage)
        searchedCase = null

        List<Case> searchedCases = actionDelegate.findCases(field)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findCases(Field)")
        assertNotNull(searchedCases, errorMessage)
        assertFalse(searchedCases.empty, errorMessage)
        assertEquals(searchedCases.size(), sizeToCheck, errorMessage)
        if (singleValueField) {
            assertEquals(searchedCases[0].stringId, firstCaseId, errorMessage)
        }
        searchedCases = null


        DataField dataField = testCase.getDataField(fieldId)
        searchedCase = actionDelegate.findCase(dataField)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findCase(DataField)")
        assertNotNull(searchedCase, errorMessage)
        assertEquals(searchedCase.stringId, firstCaseId, errorMessage)

        searchedCases = actionDelegate.findCases(dataField)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findCases(DataField)")
        assertNotNull(searchedCases, errorMessage)
        assertFalse(searchedCases.empty, errorMessage)
        assertEquals(searchedCases.size(), sizeToCheck, errorMessage)
        if (singleValueField) {
            assertEquals(searchedCases[0].stringId, firstCaseId, errorMessage)
        }
    }

    private PetriNet importTestPetriNet() {
        return petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/NAE-2390_action_api_improvements.xml"), VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
    }
}
