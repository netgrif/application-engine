package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Author
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.domain.ElasticPetriNet
import com.netgrif.application.engine.elastic.domain.ElasticPetriNetRepository
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.PetriNetSearch
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.domain.version.Version
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.LocalDateTime

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PetriNetServiceTest {

    public static final String NET_FILE = "process_delete_test.xml"
    public static final String NET_SEARCH_FILE = "process_search_test.xml"
    public static final String CUSTOMER_USER_MAIL = "customer@netgrif.com"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private IProcessRoleService userProcessRoleService

    @Autowired
    private IUserService userService

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private UriService uriService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private ProcessRoleRepository processRoleRepository

    @Autowired
    private ElasticPetriNetRepository elasticPetriNetRepository


    private static InputStream stream(String name) {
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        importHelper.createUser(new User(name: "Customer", surname: "User", email: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
    }

    @Test
    void processImportAndDelete() {
        long processRoleCount = processRoleRepository.count()
        long processCount = petriNetRepository.count()
        long taskCount = taskRepository.count()

        ImportPetriNetEventOutcome testNetOptional = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNetOptional.getNet() != null
        assert petriNetRepository.count() == processCount + 1
        PetriNet testNet = testNetOptional.getNet()
        Thread.sleep(5000)
        ElasticPetriNet elasticTestNet = elasticPetriNetRepository.findByStringId(testNet.stringId)
        assert elasticTestNet != null && elasticTestNet.getUriNodeId() == uriService.getRoot().id.toString()
        assert testNet.getUriNodeId() == uriService.getRoot().id.toString()
        assert petriNetRepository.findById(testNet.stringId).get().uriNodeId != null
        importHelper.createCase("Case 1", testNet)

        assert caseRepository.findAllByProcessIdentifier(testNet.getImportId()).size() == 1
        assert taskRepository.count() == taskCount + 3
        assert processRoleRepository.count() == processRoleCount + 2

        def user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 1

        userService.addRole(user, testNet.roles.values().collect().get(0).stringId)
        user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 2
        assert petriNetService.get(new ObjectId(testNet.stringId)) != null

        petriNetService.deletePetriNet(testNet.stringId, superCreator.getLoggedSuper())
        assert petriNetRepository.count() == processCount
        Thread.sleep(5000)
        assert elasticPetriNetRepository.findByStringId(testNet.stringId) == null
        assert caseRepository.findAllByProcessIdentifier(testNetOptional.getNet().getImportId()).size() == 0
        assert taskRepository.count() == taskCount
        assert processRoleRepository.count() == processRoleCount
        user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 1

        boolean exceptionThrown = false
        try {
            petriNetService.get(new ObjectId(testNet.stringId))
        } catch (IllegalArgumentException e) {
            exceptionThrown = true
            assert e.message.contains(testNet.stringId)
        }
        assert exceptionThrown
    }

    @Test
    void findAllByUriNodeIdTest() {
        UriNode myNode = uriService.getOrCreate("/test", UriContentType.DEFAULT)
        petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper(), myNode.id.toString())
        petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper(), myNode.id.toString())

        Thread.sleep(2000)

        List<PetriNet> petriNets = petriNetService.findAllByUriNodeId(myNode.id.toString())
        assert petriNets.size() == 2
    }

    @Test
    void processSearch() {
        long processCount = petriNetRepository.count()

        def user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        petriNetService.importPetriNet(stream(NET_SEARCH_FILE), VersionType.MAJOR, user.transformToLoggedUser())

        assert petriNetRepository.count() == processCount + 2

        PetriNetSearch search = new PetriNetSearch();
        assert petriNetService.search(search, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == processCount + 2;

        PetriNetSearch search1 = new PetriNetSearch();
        search1.setIdentifier("processSearchTest");
        assert petriNetService.search(search1, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1;

        PetriNetSearch search2 = new PetriNetSearch();
        search2.setTitle("Process Search Test");
        assert petriNetService.search(search2, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1;

        PetriNetSearch search3 = new PetriNetSearch();
        search3.setDefaultCaseName("Process Search Case Name");
        assert petriNetService.search(search3, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1;

        PetriNetSearch search4 = new PetriNetSearch();
        search4.setInitials("PST");
        assert petriNetService.search(search4, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1;


        PetriNetSearch search5 = new PetriNetSearch();
        Author author = new Author();
        author.setEmail(user.getEmail());
        search5.setAuthor(author);
        assert petriNetService.search(search5, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1;


        PetriNetSearch search6 = new PetriNetSearch();
        search6.setVersion(new Version(1,0,0));
        assert petriNetService.search(search6, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == processCount + 2;

        PetriNetSearch search7 = new PetriNetSearch();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("test", "test");
        search7.setTags(map);
        assert petriNetService.search(search7, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1;

        PetriNetSearch search8 = new PetriNetSearch();
        HashMap<String, String> mapTags = new HashMap<String, String>();
        mapTags.put("test", "test");
        search8.setTags(mapTags);
        search8.setIdentifier("processSearchTest");
        search8.setTitle("Process Search Test");
        search8.setDefaultCaseName("Process Search Case Name");
        search8.setInitials("PST");
        search8.setAuthor(author);
        assert petriNetService.search(search8, superCreator.getLoggedSuper(), PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1;
    }
}
