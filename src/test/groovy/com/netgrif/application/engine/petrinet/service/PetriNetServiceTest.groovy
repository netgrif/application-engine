package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.IdentityState
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.domain.Actor
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.authorization.service.interfaces.IActorService
import com.netgrif.application.engine.elastic.domain.ElasticPetriNet
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticPetriNetRepository
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.*
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.authorization.domain.repositories.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.domain.version.Version
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

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
    private IRoleService roleService

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private UriService uriService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private IIdentityService identityService

    @Autowired
    private IActorService actorService

    @Autowired
    private ProcessRoleRepository roleRepository

    @Autowired
    private ElasticPetriNetRepository elasticPetriNetRepository

    private Identity testIdentity;


    private static InputStream stream(String name) {
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
        testIdentity = identityService.createWithDefaultActor(IdentityParams.with()
                .username(new TextField(CUSTOMER_USER_MAIL))
                .password(new TextField("password"))
                .firstname(new TextField("Customer"))
                .lastname(new TextField("Identity"))
                .build())
    }

    @Test
    void processImportAndDelete() {
        long roleCount = roleRepository.count()
        long processCount = petriNetRepository.count()
        long taskCount = taskRepository.count()

        ImportPetriNetEventOutcome testNetOptional = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId)
        assert testNetOptional.getNet() != null
        assert petriNetRepository.count() == processCount + 1
        Process testNet = testNetOptional.getNet()
        Thread.sleep(5000)
        ElasticPetriNet elasticTestNet = elasticPetriNetRepository.findByStringId(testNet.stringId)
        assert elasticTestNet != null && elasticTestNet.getUriNodeId() == uriService.getRoot().id.toString()
        assert testNet.getUriNodeId() == uriService.getRoot().id.toString()
        assert petriNetRepository.findById(testNet.stringId).get().uriNodeId != null
        testHelper.login(superCreator.getSuperIdentity())
        importHelper.createCase("Case 1", testNet)

        assert caseRepository.findAllByProcessIdentifier(testNet.getImportId()).size() == 1
        assert taskRepository.count() == taskCount + 3
        assert roleRepository.count() == roleCount + 2

        // todo 2058
//        assert user.roles.size() == 1

//        userService.addRole(user, testNet.roles.values().collect().get(0).stringId)
//        user = userService.findByEmail(CUSTOMER_USER_MAIL)
//        assert user != null
//        assert user.roles.size() == 2
        assert petriNetService.get(new ObjectId(testNet.stringId)) != null

        petriNetService.deletePetriNet(testNet.stringId)
        assert petriNetRepository.count() == processCount
        Thread.sleep(5000)
        assert elasticPetriNetRepository.findByStringId(testNet.stringId) == null
        assert caseRepository.findAllByProcessIdentifier(testNet.getImportId()).size() == 0
        assert taskRepository.count() == taskCount
//        user = userService.findByEmail(CUSTOMER_USER_MAIL)
//        assert user != null
//        assert user.roles.size() == 1

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
        petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId,
                myNode.id.toString())
        petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId,
                myNode.id.toString())

        Thread.sleep(2000)

        List<Process> petriNets = petriNetService.findAllByUriNodeId(myNode.id.toString())
        assert petriNets.size() == 2
    }

    @Test
    void processSearch() {
        int processCount = (int) petriNetRepository.count()

        petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId)
        petriNetService.importPetriNet(stream(NET_SEARCH_FILE), VersionType.MAJOR, testIdentity.toSession().activeActorId)

        assert petriNetRepository.count() == processCount + 2

        PetriNetSearch search = new PetriNetSearch()
        assert petriNetService.search(search, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == processCount + 2

        PetriNetSearch search1 = new PetriNetSearch()
        search1.setIdentifier("processSearchTest")
        assert petriNetService.search(search1, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1

        PetriNetSearch search2 = new PetriNetSearch()
        search2.setTitle("Process Search Test")
        assert petriNetService.search(search2, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1

        PetriNetSearch search3 = new PetriNetSearch()
        search3.setDefaultCaseName("Process Search Case Name")
        assert petriNetService.search(search3, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1

        PetriNetSearch search4 = new PetriNetSearch()
        search4.setInitials("PST")
        assert petriNetService.search(search4, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1

        PetriNetSearch search5 = new PetriNetSearch()
        Optional<Actor> actorOpt = actorService.findById(testIdentity.toSession().activeActorId)
        assert actorOpt.isPresent()
        search5.setAuthor(actorOpt.get())
        assert petriNetService.search(search5, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1

        PetriNetSearch search6 = new PetriNetSearch()
        search6.setVersion(new Version(1, 0, 0))
        assert petriNetService.search(search6, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == processCount + 2

        PetriNetSearch search7 = new PetriNetSearch()
        HashMap<String, String> map = new HashMap<String, String>()
        map.put("test", "test")
        search7.setTags(map)
        assert petriNetService.search(search7, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1

        PetriNetSearch search8 = new PetriNetSearch()
        HashMap<String, String> mapTags = new HashMap<String, String>()
        mapTags.put("test", "test")
        search8.setTags(mapTags)
        search8.setIdentifier("processSearchTest")
        search8.setTitle("Process Search Test")
        search8.setDefaultCaseName("Process Search Case Name")
        search8.setInitials("PST")
        search8.setAuthor(actorOpt.get())
        assert petriNetService.search(search8, PageRequest.of(0, 50), LocaleContextHolder.locale).getNumberOfElements() == 1
    }

    @Test
    void deleteParentPetriNet() {
        Process superParentNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/super_parent_to_be_extended.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId).getNet()
        Case superParentCase = importHelper.createCaseAsSuper("Super parent case", superParentNet)

        Process parentNetMajor = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/parent_to_be_extended.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId).getNet()
        Case parentMajorCase = importHelper.createCaseAsSuper("Parent major case", parentNetMajor)

        Process parentNetMinor = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/parent_to_be_extended.xml"),
                VersionType.MINOR, superCreator.getLoggedSuper().activeActorId).getNet()
        Case parentMinorCase = importHelper.createCaseAsSuper("Parent minor case", parentNetMinor)

        Process childNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/child_extending_parent.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId).getNet()
        Case parentChildCase = importHelper.createCaseAsSuper("Child case", childNet)

        testHelper.login(superCreator.getSuperIdentity())

        petriNetService.deletePetriNet(parentNetMajor.stringId)
        assert petriNetRepository.findById(superParentNet.stringId).isPresent()
        assert petriNetRepository.findById(parentNetMajor.stringId).isEmpty()
        assert petriNetRepository.findById(parentNetMinor.stringId).isPresent()
        assert petriNetRepository.findById(childNet.stringId).isPresent()
        assert caseRepository.findById(superParentCase.stringId).isPresent()
        assert caseRepository.findById(parentMajorCase.stringId).isEmpty()
        assert caseRepository.findById(parentMinorCase.stringId).isPresent()
        assert caseRepository.findById(parentChildCase.stringId).isPresent()

        petriNetService.deletePetriNet(parentNetMinor.stringId)
        assert petriNetRepository.findById(superParentNet.stringId).isPresent()
        assert petriNetRepository.findById(parentNetMinor.stringId).isEmpty()
        assert petriNetRepository.findById(childNet.stringId).isEmpty()
        assert caseRepository.findById(superParentCase.stringId).isPresent()
        assert caseRepository.findById(parentMinorCase.stringId).isEmpty()
        assert caseRepository.findById(parentChildCase.stringId).isEmpty()

        petriNetService.deletePetriNet(superParentNet.stringId)
        assert petriNetRepository.findById(superParentNet.stringId).isEmpty()
        assert caseRepository.findById(superParentCase.stringId).isEmpty()
    }
}
