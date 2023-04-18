package com.netgrif.application.engine.elastic

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.MockService
import com.netgrif.application.engine.auth.web.responsebodies.UserFactory
import com.netgrif.application.engine.elastic.domain.ElasticCase
import com.netgrif.application.engine.elastic.domain.ElasticTask
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.context.WebApplicationContext

import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Slf4j
@SpringBootTest()
@ActiveProfiles(["test"])
@CompileStatic
@ExtendWith(SpringExtension.class)
class DataSearchRequestTest extends EngineTest {

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private IElasticIndexService template

    @Autowired
    private MockService mockService

    private List<List<String>> testCases

    @BeforeEach
    void before() {
        truncateDbs()

        template.createIndex(ElasticCase.class)
        template.putMapping(ElasticCase.class)

        template.createIndex(ElasticTask.class)
        template.putMapping(ElasticTask.class)

        elasticCaseRepository.deleteAll()

        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null

        def users = userService.findAll(true)
        assert users.size() >= 2
        def testUser1 = users[0]
        def testUser2 = users[1]
        // saving authorities / roles crashes the workflowService (on case save)
        testUser1.processRoles = new HashSet<>()
        testUser1.authorities = new HashSet<>()
        testUser2.processRoles = new HashSet<>()
        testUser2.authorities = new HashSet<>()

        LocalDate date = LocalDate.of(2020, 7, 25);
        Case _case = importHelper.createCase("correct", net.getNet())
        (_case.dataSet.get("number") as NumberField).rawValue = 7.0 as Double
        (_case.dataSet.get("boolean") as BooleanField).rawValue = true
        (_case.dataSet.get("text") as TextField).rawValue = "hello world" as String
        (_case.dataSet.get("user") as UserField).rawValue = new UserFieldValue(testUser1.stringId, testUser1.name, testUser1.surname, testUser1.email)
        (_case.dataSet.get("date") as DateField).rawValue = date
        (_case.dataSet.get("datetime") as DateTimeField).rawValue = date.atTime(13, 37)
        (_case.dataSet.get("enumeration") as EnumerationField).rawValue = (_case.petriNet.dataSet.get("enumeration") as ChoiceField).choices.find({ it.defaultValue == "Alice" })
        (_case.dataSet.get("multichoice") as MultichoiceField).rawValue = (_case.petriNet.dataSet.get("multichoice") as ChoiceField).choices.findAll({ it.defaultValue == "Alice" || it.defaultValue == "Bob" }).toSet()
        (_case.dataSet.get("enumeration_map") as EnumerationMapField).rawValue = "alice"
        (_case.dataSet.get("multichoice_map") as MultichoiceMapField).rawValue = ["alice", "bob"].toSet()
        (_case.dataSet.get("file") as FileField).rawValue = FileFieldValue.fromString("singlefile.txt")
        (_case.dataSet.get("fileList") as FileListField).rawValue = FileListFieldValue.fromString("multifile1.txt,multifile2.pdf")
        (_case.dataSet.get("userList") as UserListField).rawValue = new UserListFieldValue([dataService.makeUserFieldValue(testUser1.stringId), dataService.makeUserFieldValue(testUser2.stringId)])
        (_case.dataSet.get("i18n_text") as I18nField).rawValue.defaultValue = "Modified i18n text value"
        (_case.dataSet.get("i18n_divider") as I18nField).rawValue.defaultValue = "Modified i18n divider value"
        workflowService.save(_case)

        Task actionTrigger = taskService.searchOne(QTask.task.caseId.eq(_case.stringId).and(QTask.task.transitionId.eq("2")));
        assert actionTrigger != null
        dataService.setData(actionTrigger, new DataSet([
                "testActionTrigger": new TextField(rawValue: "random value")
        ] as Map<String, Field<?>>), superCreator.getSuperUser())

        10.times {
            _case = importHelper.createCase("wrong${it}", net.getNet())
            workflowService.save(_case)
        }

        testCases = [
                ["number" as String, "7.0" as String],
                ["number.numberValue" as String, "7" as String],
                ["boolean" as String, "true" as String],
                ["boolean.booleanValue" as String, "true" as String],
                ["text" as String, "hello world" as String],
                ["text.textValue.keyword" as String, "hello world" as String],
                ["user" as String, "${testUser1.fullName} ${testUser1.email}" as String],
                ["user.emailValue.keyword" as String, "${testUser1.email}" as String],
                ["user.fullNameValue.keyword" as String, "${testUser1.fullName}" as String],
                ["user.userIdValue" as String, "${testUser1.getStringId()}" as String],
                ["date.timestampValue" as String, "${Timestamp.valueOf(LocalDateTime.of(date, LocalTime.NOON)).getTime()}" as String],
                ["datetime.timestampValue" as String, "${Timestamp.valueOf(date.atTime(13, 37)).getTime()}" as String],
                ["enumeration" as String, "Alice" as String],
                ["enumeration" as String, "Alica" as String],
                ["enumeration.textValue.keyword" as String, "Alice" as String],
                ["enumeration.textValue.keyword" as String, "Alica" as String],
                ["multichoice" as String, "Alice" as String],
                ["multichoice" as String, "Alica" as String],
                ["multichoice" as String, "Bob" as String],
                ["multichoice" as String, "Bobek" as String],
                ["multichoice.textValue.keyword" as String, "Alice" as String],
                ["multichoice.textValue.keyword" as String, "Alica" as String],
                ["multichoice.textValue.keyword" as String, "Bob" as String],
                ["multichoice.textValue.keyword" as String, "Bobek" as String],
                ["enumeration_map" as String, "Alice" as String],
                ["enumeration_map" as String, "Alica" as String],
                ["enumeration_map.textValue.keyword" as String, "Alice" as String],
                ["enumeration_map.textValue.keyword" as String, "Alica" as String],
                ["enumeration_map.keyValue" as String, "alice" as String],
                ["multichoice_map" as String, "Alice" as String],
                ["multichoice_map" as String, "Alica" as String],
                ["multichoice_map" as String, "Bob" as String],
                ["multichoice_map" as String, "Bobek" as String],
                ["multichoice_map.textValue.keyword" as String, "Alice" as String],
                ["multichoice_map.textValue.keyword" as String, "Alica" as String],
                ["multichoice_map.textValue.keyword" as String, "Bob" as String],
                ["multichoice_map.textValue.keyword" as String, "Bobek" as String],
                ["multichoice_map.keyValue" as String, "alice" as String],
                ["multichoice_map.keyValue" as String, "bob" as String],
                ["file" as String, "singlefile.txt" as String],
                ["file.fileNameValue.keyword" as String, "singlefile" as String],
                ["file.fileExtensionValue.keyword" as String, "txt" as String],
                ["fileList" as String, "multifile1.txt" as String],
                ["fileList" as String, "multifile2.pdf" as String],
                ["fileList.fileNameValue.keyword" as String, "multifile1" as String],
                ["fileList.fileNameValue.keyword" as String, "multifile2" as String],
                ["fileList.fileExtensionValue.keyword" as String, "txt" as String],
                ["fileList.fileExtensionValue.keyword" as String, "pdf" as String],
                ["userList" as String, "${testUser1.fullName} ${testUser1.email}" as String],
                ["userList" as String, "${testUser2.fullName} ${testUser2.email}" as String],
                ["userList.emailValue.keyword" as String, "${testUser1.email}" as String],
                ["userList.emailValue.keyword" as String, "${testUser2.email}" as String],
                ["userList.fullNameValue.keyword" as String, "${testUser1.fullName}" as String],
                ["userList.fullNameValue.keyword" as String, "${testUser2.fullName}" as String],
                ["userList.userIdValue" as String, "${testUser1.getStringId()}" as String],
                ["userList.userIdValue" as String, "${testUser2.getStringId()}" as String],
                ["enumeration_map_changed" as String, "Eve" as String],
                ["enumeration_map_changed" as String, "Eva" as String],
                ["enumeration_map_changed.textValue.keyword" as String, "Eve" as String],
                ["enumeration_map_changed.textValue.keyword" as String, "Eva" as String],
                ["enumeration_map_changed.keyValue" as String, "eve" as String],
                ["multichoice_map_changed" as String, "Eve" as String],
                ["multichoice_map_changed" as String, "Eva" as String],
                ["multichoice_map_changed" as String, "Felix" as String],
                ["multichoice_map_changed" as String, "Félix" as String],
                ["multichoice_map_changed.textValue.keyword" as String, "Eve" as String],
                ["multichoice_map_changed.textValue.keyword" as String, "Eva" as String],
                ["multichoice_map_changed.textValue.keyword" as String, "Felix" as String],
                ["multichoice_map_changed.textValue.keyword" as String, "Félix" as String],
                ["multichoice_map_changed.keyValue" as String, "eve" as String],
                ["multichoice_map_changed.keyValue" as String, "felix" as String],
                ["i18n_text.textValue.keyword" as String, "Modified i18n text value" as String],
                ["i18n_divider.textValue.keyword" as String, "Modified i18n divider value" as String],
        ]
    }

    @Test
    void testDataSearchRequests() {
        testCases.each { testCase ->
            String field = testCase[0]
            String term = testCase[1]

            CaseSearchRequest request = new CaseSearchRequest()
            request.data = new HashMap<>()
            request.data.put(field, term)

            log.info(String.format("Testing %s == %s", field, term))

            Page<Case> result = elasticCaseService.search([request] as List, mockService.mockLoggedUser(), PageRequest.of(0, 100), null, false)
            assert result.size() == 1
        }
    }
}
