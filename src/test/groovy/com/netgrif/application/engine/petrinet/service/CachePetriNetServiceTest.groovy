package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.core.auth.domain.Authority;
import com.netgrif.core.auth.domain.User
import com.netgrif.core.auth.domain.enums.UserState
import com.netgrif.adapter.auth.service.UserService
import com.netgrif.application.engine.configuration.properties.CacheProperties
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.core.petrinet.domain.VersionType
import com.netgrif.adapter.petrinet.service.PetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.core.petrinet.domain.roles.ProcessRole
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
    private PetriNetService petriNetService

    @Autowired
    private UserService userService

    @Autowired
    private CacheManager cacheManager

    @Autowired
    private CacheProperties cacheProperties;

    private def stream = { String name ->
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
    void cacheTest() {
        assert cacheManager.getCache(cacheProperties.getPetriNetNewest()).get("processDeleteTest") == null
        ImportPetriNetEventOutcome testNetOptional = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNetOptional.getNet() != null
        PetriNet testNet = testNetOptional.getNet()

        assert cacheManager.getCache(cacheProperties.getPetriNetNewest()).get(testNet.getIdentifier()) == null
        PetriNet test = petriNetService.getNewestVersionByIdentifier(testNet.getIdentifier())
        assert cacheManager.getCache(cacheProperties.getPetriNetNewest()).get(testNet.getIdentifier()).get().equals(test)
    }
}
