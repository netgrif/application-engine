package com.netgrif.workflow.elastic

import com.netgrif.workflow.MockService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.elastic.domain.ElasticCase
import com.netgrif.workflow.elastic.domain.ElasticCaseRepository
import com.netgrif.workflow.elastic.domain.ElasticTask
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService
import com.netgrif.workflow.elastic.web.CaseSearchRequest
import com.netgrif.workflow.importer.service.Config
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.startup.ImportHelper
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
    private Importer importer

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

    private Map<String, String> testCases

    @Before
    void before() {
        template.deleteIndex(ElasticCase.class)
        template.createIndex(ElasticCase.class)
        template.deleteIndex(ElasticTask.class)
        template.createIndex(ElasticTask.class)
        template.putMapping(ElasticCase.class)
        template.putMapping(ElasticTask.class)

        repository.deleteAll()

        def net = importer.importPetriNet(new File("src/test/resources/all_data.xml"), PROCESS_TITLE, PROCESS_INITIALS, new Config())
        assert net.isPresent()

        def users = userService.findAll(true)
        assert users.size() > 0
        def testUser = users[0]
        // saving authorities / roles crashes the workflowService
        testUser.userProcessRoles = []
        testUser.authorities = []

        LocalDate date = LocalDate.of(2020, 7, 25);
        Case _case = importHelper.createCase("correct", net.get())
        _case.dataSet["number"].value = 7.0 as Double
        _case.dataSet["boolean"].value = true
        _case.dataSet["text"].value = "hello world" as String
        _case.dataSet["user"].value = testUser
        _case.dataSet["date"].value = date
        _case.dataSet["datetime"].value = date.atTime(13, 37)
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
            ("user.userIdValue" as String) : "${testUser.getId()}"  as String,
            ("date.timestampValue" as String) : "${Timestamp.valueOf(LocalDateTime.of(date, LocalTime.NOON)).getTime()}"  as String,
            ("datetime.timestampValue" as String) : "${Timestamp.valueOf(date.atTime(13, 37)).getTime()}"  as String
        ]
    }

    @Test
    void testDatSearchRequests() {
        testCases.entrySet().each { searchRequest ->
            CaseSearchRequest request = new CaseSearchRequest()
            request.data = new HashMap<>()
            request.data.put(searchRequest.getKey(), searchRequest.getValue())

            log.info(String.format("Testing %s", searchRequest.getKey()))

            Page<Case> result = searchService.search(request, mockService.mockLoggedUser(), PageRequest.of(0, 100))
            assert result.size() == 1
        }
    }
}
