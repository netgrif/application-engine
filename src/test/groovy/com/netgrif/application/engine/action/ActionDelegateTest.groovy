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
import com.netgrif.application.engine.workflow.service.interfaces.IFilterImportExportService
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import javax.mail.internet.MimeMessage

import static java.util.Base64.*
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ActionDelegateTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ActionDelegate actionDelegate

    @Autowired
    private IFilterImportExportService importExportService

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
    @Disabled("Context user")
    void importFiltersTest() {
        List<String> actionDelegateList = actionDelegate.importFilters()
        List<String> importedTasksIds = importExportService.importFilters()
        assert actionDelegateList.size() == importedTasksIds.size()
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

        assert tasks != null
        assert !tasks.empty
        assert tasks.size() == taskIds.size()

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
        assert searchedCase != null
        assert searchedCase.stringId == testCase1.stringId

        List<Case> searchedCases = actionDelegate.findCases(caseIds)
        assert searchedCases != null
        assert !searchedCases.empty
        assert searchedCases.size() == caseIds.size()


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
        assert foundNet != null
        assert foundNet.objectId == objectId
        assert foundNet.identifier == netIdentifier
        foundNet = null

        foundNet = actionDelegate.findPetriNet(objectId)
        assert foundNet != null
        assert foundNet.stringId == mongoId
        assert foundNet.identifier == netIdentifier
        foundNet = null

        PetriNet filterNet = petriNetService.getByIdentifier("filter")[0]
        String mongoId2 = filterNet.stringId
        String netIdentifier2 = filterNet.getIdentifier()
        ObjectId objectId3 = filterNet.objectId

        def searchTargets = [mongoId, mongoId2]
        List<PetriNet> searchResults = actionDelegate.findPetriNets(searchTargets as List<String>)
        assert searchResults != null
        assert !searchResults.empty
        assert searchResults.collect { it.stringId }.containsAll(searchTargets)
        searchResults = null

        searchTargets = [objectId, objectId]
        searchResults = actionDelegate.findPetriNetsByObjectIds(searchTargets as List<ObjectId>)
        assert searchResults != null
        assert !searchResults.empty
        assert searchResults.collect { it.objectId }.containsAll(searchTargets)

        PetriNet importedNet2 = importTestPetriNet()
        assert importedNet2 != null
        assert importedNet2.identifier == netIdentifier
        assert importedNet2.stringId != mongoId
        assert importedNet2.version != importedNet.version

        foundNet = actionDelegate.findPetriNetByIdentifier(importedNet2.identifier, new Version(1, 0, 0))
        assert foundNet != null
        assert foundNet.identifier == netIdentifier
        assert foundNet.stringId == mongoId
        assert foundNet.version == importedNet.version
        assert foundNet.version != importedNet2.version
        foundNet = null

//        find newest
        foundNet = actionDelegate.findPetriNetByIdentifier(importedNet2.identifier)
        assert foundNet != null
        assert foundNet.identifier == netIdentifier
        assert foundNet.stringId != mongoId
        assert foundNet.version != importedNet.version
        assert foundNet.version == importedNet2.version
    }

    @Test
    void testOptionsActions() {
        PetriNet net = importTestPetriNet()
        Case case1 = actionDelegate.createCase(net, "Test title 1")
        Case case2 = actionDelegate.createCase(net, "Test title 2")

        List<Case> cases = [case1, case2]
        Map<String, I18nString> options = actionDelegate.casesToOptions(cases)
        assert options != null
        assert options.size() == cases.size()
        assert options.keySet().containsAll(cases.collect { it.stringId })
        assert options.get(case1.stringId).defaultValue == case1.title
        assert options.get(case2.stringId).defaultValue == case2.title
        options = null

        String keyTransformationTestString = "Key transformation test "
        String valueTransformationTestString = "Value transformation test "
        options = actionDelegate.casesToOptions(cases, { return "Value transformation test ".concat(it.title) }, { return "Key transformation test ".concat(it.stringId) })
        assert options != null
        assert options.size() == cases.size()
        assert options.keySet().containsAll(cases.collect { keyTransformationTestString.concat(it.stringId) })
        assert options.get(keyTransformationTestString.concat(case1.stringId)).defaultValue == valueTransformationTestString.concat(case1.title)
        assert options.get(keyTransformationTestString.concat(case2.stringId)).defaultValue == valueTransformationTestString.concat(case2.title)

        case1.dataSet["enumeration_map"].options = options
        actionDelegate.useCase = case1
        actionDelegate.initFieldsMap(["enumeration_map": "enumeration_map"])
        EnumerationMapField field = (EnumerationMapField) actionDelegate.map.get("enumeration_map")
        assert field != null
        assert field.options != null
        I18nString option = field.getOption(keyTransformationTestString.concat(case1.stringId))
        assert option != null
        assert option.defaultValue == valueTransformationTestString.concat(case1.title)
    }

    @Test
    void testTaskEventActions() {
        importTestPetriNet()
        Case testCase = actionDelegate.createCase(ACTION_API_NET_IDENTIFIER)
        List<String> transitionIds = ["t1", "t2"]
        IUser user = userService.getLoggedOrSystem()

//        testing "byTransition" variants should be enough, as they call methods, that take List<Task> instead of List<String>
        List<Task> tasks = actionDelegate.assignTasksByTransitions(transitionIds, testCase)
        assert tasks != null
        assert tasks.size() == transitionIds.size()
        assert tasks.stream().allMatch { it.user.email == user.email }
        assert tasks.stream().allMatch { it.userId == user.stringId }
        assert tasks.stream().allMatch { it.startDate != null }
        tasks = null

        tasks = actionDelegate.cancelTasksByTransitions(transitionIds, testCase)
        assert tasks != null
        assert tasks.size() == transitionIds.size()
        assert tasks.stream().allMatch { it.userId == null }
        assert tasks.stream().allMatch { it.startDate == null }
        tasks = null

        actionDelegate.assignTasksByTransitions(transitionIds, testCase)
        tasks = actionDelegate.finishTasksByTransitions(transitionIds, testCase)
        assert tasks != null
        assert tasks.size() == transitionIds.size()
        assert tasks.stream().allMatch { it.finishedBy == user.stringId }
        assert tasks.stream().allMatch { it.finishDate != null }
        assert tasks.stream().allMatch { it.userId == null }
        tasks = actionDelegate.findTasks(tasks.collect { it.stringId })
        assert tasks.size() == 1
        assert tasks[0].transitionId == "t2"
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
        assert task != null : errorMessage
        assert task.stringId == firstTaskId : errorMessage
        task = null

        List<Task> tasks = actionDelegate.findTasks(field)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findTasks(Field)")
        assert tasks != null : errorMessage
        assert !tasks.empty : errorMessage
        assert tasks.size() == sizeToCheck : errorMessage
        if (singleValueField) {
            assert tasks[0].stringId == firstTaskId : errorMessage
        }
        tasks = null

        DataField dataField = testCase.getDataField(fieldId)
        task = actionDelegate.findTask(dataField)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findTask(DataField)")
        assert task != null : errorMessage
        assert task.stringId == firstTaskId : errorMessage

        tasks = actionDelegate.findTasks(dataField)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findTasks(DataField)")
        assert tasks != null : errorMessage
        assert !tasks.empty : errorMessage
        assert tasks.size() == sizeToCheck : errorMessage
        if (singleValueField) {
            assert tasks[0].stringId == firstTaskId : errorMessage
        }
    }

    private void assertCaseSearchResults(String fieldId, Case testCase, int sizeToCheck, boolean singleValueField) {
        actionDelegate.initFieldsMap([(fieldId): fieldId])
        String errorMessageWithFieldId = ERROR_MESSAGE_TEMPLATE.replace(FIELD_ID_TEMPLATE, fieldId)
        Field field = actionDelegate.map.get(fieldId)
        String firstCaseId = ([field.value].flatten() as List<String>)[0]

        Case searchedCase = actionDelegate.findCase(field)
        String errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findCase(Field)")
        assert searchedCase != null : errorMessage
        assert searchedCase.stringId == firstCaseId : errorMessage
        searchedCase = null

        List<Case> searchedCases = actionDelegate.findCases(field)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findCases(Field)")
        assert searchedCases != null : errorMessage
        assert !searchedCases.empty : errorMessage
        assert searchedCases.size() == sizeToCheck : errorMessage
        if (singleValueField) {
            assert searchedCases[0].stringId == firstCaseId : errorMessage
        }
        searchedCases = null


        DataField dataField = testCase.getDataField(fieldId)
        searchedCase = actionDelegate.findCase(dataField)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findCase(DataField)")
        assert searchedCase != null: errorMessage
        assert searchedCase.stringId == firstCaseId: errorMessage

        searchedCases = actionDelegate.findCases(dataField)
        errorMessage = errorMessageWithFieldId.replace(TESTED_METHOD_TEMPLATE, "findCases(DataField)")
        assert searchedCases != null: errorMessage
        assert !searchedCases.empty: errorMessage
        assert searchedCases.size() == sizeToCheck: errorMessage
        if (singleValueField) {
            assert searchedCases[0].stringId == firstCaseId: errorMessage
        }
    }

    private PetriNet importTestPetriNet() {
        return petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/NAE-2390_action_api_improvements.xml"), VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
    }
}
