package com.netgrif.workflow.elastic

import com.netgrif.workflow.MockService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.elastic.domain.ElasticCase
import com.netgrif.workflow.elastic.domain.ElasticCaseRepository
import com.netgrif.workflow.elastic.domain.ElasticTask
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue
import com.netgrif.workflow.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.context.WebApplicationContext

import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest()
class DataSearchRequestTest {

    private static final Logger log = LoggerFactory.getLogger(DataSearchRequestTest)

    public static final String PROCESS_TITLE = "Elastic data search request test"
    public static final String PROCESS_INITIALS = "TST"

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private ElasticCaseRepository repository

    @Autowired
    private ElasticsearchTemplate template

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IUserService userService

    @Autowired
    private MockService mockService

    @Autowired
    private IElasticCaseService searchService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    private Map<String, String> testCases

    @Before
    void before() {
        template.deleteIndex(ElasticCase.class)
        template.createIndex(ElasticCase.class)
        template.putMapping(ElasticCase.class)

        template.deleteIndex(ElasticTask.class)
        template.createIndex(ElasticTask.class)
        template.putMapping(ElasticTask.class)

        repository.deleteAll()

        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), "major", superCreator.getLoggedSuper())
        assert net.isPresent()

        def users = userService.findAll(true)
        assert users.size() >= 2
        def testUser1 = users[0]
        def testUser2 = users[1]
        // saving authorities / roles crashes the workflowService (on case save)
        testUser1.userProcessRoles = []
        testUser1.authorities = []
        testUser2.userProcessRoles = []
        testUser2.authorities = []

        LocalDate date = LocalDate.of(2020, 7, 25);
        Case _case = importHelper.createCase("correct", net.get())
        _case.dataSet["number"].value = 7.0 as Double
        _case.dataSet["boolean"].value = true
        _case.dataSet["text"].value = "hello world" as String
        _case.dataSet["user"].value = testUser1
        _case.dataSet["date"].value = date
        _case.dataSet["datetime"].value = date.atTime(13, 37)
        _case.dataSet["enumeration"].value = _case.dataSet["enumeration"].choices.find({ it.defaultValue == "Alice" })
        _case.dataSet["multichoice"].value = _case.dataSet["enumeration"].choices.findAll({ it.defaultValue == "Alice" || it.defaultValue == "Bob"})
        _case.dataSet["enumeration_map"].value = "alice"
        _case.dataSet["multichoice_map"].value = ["alice", "bob"]
        _case.dataSet["file"].value = FileFieldValue.fromString("singlefile.txt")
        _case.dataSet["fileList"].value = FileListFieldValue.fromString("multifile1.txt,multifile2.pdf")
        _case.dataSet["userList"].value = [testUser1.id, testUser2.id]
        workflowService.save(_case)

        10.times {
            _case = importHelper.createCase("wrong${it}", net.get())
            workflowService.save(_case)
        }

        testCases = [
            ("number" as String) : "7.0"  as String,
            ("number.numberValue"  as String): "7"  as String,
            ("boolean" as String) : "true"  as String,
            ("boolean.booleanValue" as String) : "true" as String,
            ("text" as String) : "hello world"  as String,
            ("text.textValue.keyword"  as String) : "hello world"  as String,
            ("user" as String) : "${testUser1.fullName} ${testUser1.email}"  as String,
            ("user.emailValue" as String) : "${testUser1.email}"  as String,
            ("user.fullNameValue" as String) : "${testUser1.fullName}"  as String,
            ("user.userIdValue" as String) : "${testUser1.getId()}"  as String,
            ("date.timestampValue" as String) : "${Timestamp.valueOf(LocalDateTime.of(date, LocalTime.NOON)).getTime()}"  as String,
            ("datetime.timestampValue" as String) : "${Timestamp.valueOf(date.atTime(13, 37)).getTime()}"  as String,
            ("enumeration" as String) : "Alice"  as String,
            ("enumeration" as String) : "Alica"  as String,
            ("enumeration.textValue.keyword" as String) : "Alice"  as String,
            ("enumeration.textValue.keyword" as String) : "Alica"  as String,
            ("multichoice" as String) : "Alice"  as String,
            ("multichoice" as String) : "Alica"  as String,
            ("multichoice" as String) : "Bob"  as String,
            ("multichoice" as String) : "Bobek"  as String,
            ("multichoice.textValue.keyword" as String) : "Alice"  as String,
            ("multichoice.textValue.keyword" as String) : "Alica"  as String,
            ("multichoice.textValue.keyword" as String) : "Bob"  as String,
            ("multichoice.textValue.keyword" as String) : "Bobek"  as String,
            ("enumeration_map" as String) : "Alice"  as String,
            ("enumeration_map" as String) : "Alica"  as String,
            ("enumeration_map.textValue.keyword" as String) : "Alice"  as String,
            ("enumeration_map.textValue.keyword" as String) : "Alica"  as String,
            ("enumeration_map.keyValue" as String) : "alice"  as String,
            ("multichoice_map" as String) : "Alice"  as String,
            ("multichoice_map" as String) : "Alica"  as String,
            ("multichoice_map" as String) : "Bob"  as String,
            ("multichoice_map" as String) : "Bobek"  as String,
            ("multichoice_map.textValue.keyword" as String) : "Alice"  as String,
            ("multichoice_map.textValue.keyword" as String) : "Alica"  as String,
            ("multichoice_map.textValue.keyword" as String) : "Bob"  as String,
            ("multichoice_map.textValue.keyword" as String) : "Bobek"  as String,
            ("multichoice_map.keyValue" as String) : "alice"  as String,
            ("multichoice_map.keyValue" as String) : "bob"  as String,
            ("file" as String) : "singlefile.txt"  as String,
            ("file.fileNameValue.keyword" as String) : "singlefile"  as String,
            ("file.fileExtensionValue.keyword" as String) : "txt"  as String,
            ("fileList" as String) : "multifile1.txt"  as String,
            ("fileList" as String) : "multifile2.pdf"  as String,
            ("fileList.fileNameValue.keyword" as String) : "multifile1"  as String,
            ("fileList.fileNameValue.keyword" as String) : "multifile2"  as String,
            ("fileList.fileExtensionValue.keyword" as String) : "txt"  as String,
            ("fileList.fileExtensionValue.keyword" as String) : "pdf"  as String,
            ("userList" as String) : "${testUser1.fullName} ${testUser1.email}"  as String,
            ("userList" as String) : "${testUser2.fullName} ${testUser2.email}"  as String,
            ("userList.emailValue" as String) : "${testUser1.email}"  as String,
            ("userList.emailValue" as String) : "${testUser2.email}"  as String,
            ("userList.fullNameValue" as String) : "${testUser1.fullName}"  as String,
            ("userList.fullNameValue" as String) : "${testUser2.fullName}"  as String,
            ("userList.userIdValue" as String) : "${testUser1.getId()}"  as String,
            ("userList.userIdValue" as String) : "${testUser2.getId()}"  as String,
        ]
    }

    @Test
    void testDatSearchRequests() {
        testCases.entrySet().each { searchRequest ->
            CaseSearchRequest request = new CaseSearchRequest()
            request.data = new HashMap<>()
            request.data.put(searchRequest.getKey(), searchRequest.getValue())

            log.info(String.format("Testing %s == %s", searchRequest.getKey(), searchRequest.getValue()))

            Page<Case> result = searchService.search([request] as List, mockService.mockLoggedUser(), PageRequest.of(0, 100), null, false)
            assert result.size() == 1
        }
    }
}
