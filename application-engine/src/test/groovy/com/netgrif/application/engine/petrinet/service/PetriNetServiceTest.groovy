package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.elastic.domain.ElasticPetriNetRepository
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.objects.auth.domain.*
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.petrinet.domain.version.Version
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.params.DeletePetriNetParams
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
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

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PetriNetServiceTest {

    public static final String NET_FILE = "process_delete_test.xml"
    public static final String VERSION_PROCESS_FILE_FORMAT = "petriNets/process_version_%s_0_0.xml"
    public static final String NET_SEARCH_FILE = "process_search_test.xml"
    public static final String CUSTOMER_USER_MAIL = "customer@netgrif.com"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private ProcessRoleService userProcessRoleService

    @Autowired
    private UserService userService

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private ProcessRoleRepository processRoleRepository

    @Autowired
    private ElasticPetriNetRepository elasticPetriNetRepository


    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        importHelper.createUser(new User(firstName: "Customer", lastName: "User", email: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
    }

    @Test
    @Disabled
    void processImportAndDelete() {
        long processRoleCount = processRoleRepository.count()
        long processCount = petriNetRepository.count()
        long taskCount = taskRepository.count()


        ImportPetriNetEventOutcome testNetOptional = importProcess(NET_FILE, superCreator.getLoggedSuper())
        assert testNetOptional.getNet() != null
        assert petriNetRepository.count() == processCount + 1
        PetriNet testNet = testNetOptional.getNet()
        Thread.sleep(5000)
        assert petriNetRepository.findById(testNet.stringId).get().uriNodeId == null
        importHelper.createCase("Case 1", testNet)

        assert caseRepository.findAllByProcessIdentifier(testNet.getImportId()).size() == 1
        assert taskRepository.count() == taskCount + 2
        assert processRoleRepository.count() == processRoleCount + 2

        def user = userService.findUserByUsername(CUSTOMER_USER_MAIL, null)
        assert user != null && user.isPresent()
        assert user.get().processRoles.size() == 1

        userService.addRole(user.get(), testNet.roles.values().collect().get(0).stringId)
        user = userService.findUserByUsername(CUSTOMER_USER_MAIL, null)
        assert user != null && user.isPresent()
        assert user.get().processRoles.size() == 2
        assert petriNetService.get(new ObjectId(testNet.stringId)) != null

        petriNetService.deletePetriNet(testNet.stringId, superCreator.getLoggedSuper())
        assert petriNetRepository.count() == processCount
        Thread.sleep(5000)
        assert elasticPetriNetRepository.findById(testNet.stringId).isEmpty()
        assert caseRepository.findAllByProcessIdentifier(testNetOptional.getNet().getImportId()).size() == 0
        assert taskRepository.count() == taskCount
        assert processRoleRepository.count() == processRoleCount
        user = userService.findUserByUsername(CUSTOMER_USER_MAIL, null)
        assert user != null && user.isPresent()
        assert user.get().processRoles.size() == 1

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
    void testVersionsOnImport() {
        ImportPetriNetEventOutcome outcome = importProcess(VERSION_PROCESS_FILE_FORMAT.formatted("2"), superCreator.loggedSuper)
        PetriNet petriNetV2 = outcome.getNet()
        assertNotNull(petriNetV2)
        assertTrue(petriNetV2.defaultVersion)
        Version version = new Version()
        version.setMajor(2)
        assertTrue(petriNetV2.getVersion() == version)
        Thread.sleep(5000)
        Optional<ElasticPetriNet> elasticPetriNetV2Optional = elasticPetriNetRepository.findById(petriNetV2.stringId)
        assertTrue(elasticPetriNetV2Optional.isPresent())
        assertTrue(elasticPetriNetV2Optional.get().isDefaultVersion())

        outcome = importProcess(VERSION_PROCESS_FILE_FORMAT.formatted("4"), superCreator.loggedSuper)
        PetriNet petriNetV4 = outcome.getNet()
        assertNotNull(petriNetV4)
        assertFalse(petriNetService.get(petriNetV2.getObjectId()).isDefaultVersion())
        assertTrue(petriNetV4.isDefaultVersion())
        version = new Version()
        version.setMajor(4)
        assertTrue(petriNetV4.getVersion() == version)
        Thread.sleep(5000)
        elasticPetriNetV2Optional = elasticPetriNetRepository.findById(petriNetV2.stringId)
        assertFalse(elasticPetriNetV2Optional.get().isDefaultVersion())
        Optional<ElasticPetriNet> elasticPetriNetV4Optional = elasticPetriNetRepository.findById(petriNetV4.stringId)
        assertTrue(elasticPetriNetV4Optional.isPresent())
        assertTrue(elasticPetriNetV4Optional.get().isDefaultVersion())

        outcome = importProcess(VERSION_PROCESS_FILE_FORMAT.formatted("1"), superCreator.loggedSuper)
        PetriNet petriNetV1 = outcome.getNet()
        assertNotNull(petriNetV1)
        assertTrue(petriNetService.get(petriNetV4.getObjectId()).isDefaultVersion())
        assertFalse(petriNetV1.isDefaultVersion())
        version = new Version()
        version.setMajor(1)
        assertTrue(petriNetV1.getVersion() == version)

        assertThrows(IllegalArgumentException.class, {
            // cannot import already existing version
            importProcess(VERSION_PROCESS_FILE_FORMAT.formatted("2"), superCreator.loggedSuper)
        })

        petriNetV2.makeDefault()
        petriNetV2 = petriNetService.save(petriNetV2).get()
        assertTrue(petriNetV2.defaultVersion)
        petriNetV4.makeNonDefault()
        petriNetV4 = petriNetService.save(petriNetV4).get()
        assertFalse(petriNetV4.defaultVersion)

        outcome = importProcess(VERSION_PROCESS_FILE_FORMAT.formatted("5"), superCreator.loggedSuper)
        PetriNet petriNetV5 = outcome.getNet()
        assertNotNull(petriNetV5)
        assertFalse(petriNetService.get(petriNetV2.getObjectId()).defaultVersion)
        assertFalse(petriNetService.get(petriNetV4.getObjectId()).defaultVersion)
        assertTrue(petriNetV5.defaultVersion)
        version = new Version()
        version.setMajor(5)
        assertTrue(petriNetV5.getVersion() == version)
        Thread.sleep(5000)
        elasticPetriNetV2Optional = elasticPetriNetRepository.findById(petriNetV2.stringId)
        assertFalse(elasticPetriNetV2Optional.get().defaultVersion)
        elasticPetriNetV4Optional = elasticPetriNetRepository.findById(petriNetV4.stringId)
        assertFalse(elasticPetriNetV4Optional.get().defaultVersion)
        Optional<ElasticPetriNet> elasticPetriNetV5Optional = elasticPetriNetRepository.findById(petriNetV5.stringId)
        assertTrue(elasticPetriNetV5Optional.isPresent())
        assertTrue(elasticPetriNetV5Optional.get().defaultVersion)
    }

    @Test
    void testVersionDefaultOnDelete() {
        ImportPetriNetEventOutcome outcome = importProcess(VERSION_PROCESS_FILE_FORMAT.formatted("1"), superCreator.loggedSuper)
        PetriNet processV1 = outcome.getNet()
        outcome = importProcess(VERSION_PROCESS_FILE_FORMAT.formatted("2"), superCreator.loggedSuper)
        PetriNet processV2 = outcome.getNet()
        outcome = importProcess(VERSION_PROCESS_FILE_FORMAT.formatted("3"), superCreator.loggedSuper)
        PetriNet processV3 = outcome.getNet()

        assertFalse(petriNetService.get(processV1.getObjectId()).defaultVersion)
        assertFalse(petriNetService.get(processV2.getObjectId()).defaultVersion)
        assertTrue(petriNetService.get(processV3.getObjectId()).defaultVersion)

        petriNetService.deletePetriNet(new DeletePetriNetParams(processV2.getStringId(), superCreator.loggedSuper))

        assertTrue(petriNetRepository.findById(processV2.getStringId()).isEmpty())
        assertFalse(petriNetService.get(processV1.getObjectId()).defaultVersion)
        assertTrue(petriNetService.get(processV3.getObjectId()).defaultVersion)

        petriNetService.deletePetriNet(new DeletePetriNetParams(processV3.getStringId(), superCreator.loggedSuper))

        assertTrue(petriNetRepository.findById(processV3.getStringId()).isEmpty())
        assertTrue(petriNetService.get(processV1.getObjectId()).defaultVersion)

        petriNetService.deletePetriNet(new DeletePetriNetParams(processV1.getStringId(), superCreator.loggedSuper))

        assertTrue(petriNetRepository.findById(processV1.getStringId()).isEmpty())
    }

    @Test
    @Disabled
    void processSearch() {
        long processCount = petriNetRepository.count()

        def user = userService.findUserByUsername(CUSTOMER_USER_MAIL, null)
        assert user != null && user.isPresent()
        importProcess(NET_FILE, superCreator.getLoggedSuper())
        importProcess(NET_SEARCH_FILE, ActorTransformer.toLoggedUser(user.get()))

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
        ActorRef author = new ActorRef();
        author.setIdentifier(user.get().getUsername());
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

    private ImportPetriNetEventOutcome importProcess(String filePath, LoggedUser author) {
        return petriNetService.importPetriNet(new ImportPetriNetParams(stream(filePath), VersionType.MAJOR, author))
    }
}
