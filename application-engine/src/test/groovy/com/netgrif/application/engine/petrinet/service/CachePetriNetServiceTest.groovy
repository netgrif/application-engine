package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.configuration.properties.CacheConfigurationProperties
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class CachePetriNetServiceTest {

    public static final String NET_FILE = "process_delete_test.xml"
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
    private UserService userService

    @Autowired
    private CacheManager cacheManager

    @Autowired
    private CacheConfigurationProperties cacheProperties;

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
    void cacheTest() {
        assert cacheManager.getCache(cacheProperties.getPetriNetDefault()).get("processDeleteTest") == null
        ImportPetriNetEventOutcome testNetOptional = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNetOptional.getNet() != null
        PetriNet testNet = testNetOptional.getNet()

        assert cacheManager.getCache(cacheProperties.getPetriNetDefault()).get(testNet.getIdentifier()) == null
        PetriNet test = petriNetService.getDefaultVersionByIdentifier(testNet.getIdentifier())
        assert cacheManager.getCache(cacheProperties.getPetriNetDefault()).get(testNet.getIdentifier()).get().equals(test)
    }
}
