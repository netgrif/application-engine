package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.configuration.properties.CacheProperties
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
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
    private SuperCreator superCreator

    @Autowired
    private IPetriNetService petriNetService

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
        importHelper.createIdentity(IdentityParams.with()
                .firstname(new TextField("Customer"))
                .lastname(new TextField("Identity"))
                .username(new TextField(CUSTOMER_USER_MAIL))
                .password(new TextField("password"))
                .build(), new ArrayList<Role>())
    }

    @Test
    void cacheTest() {
        assert cacheManager.getCache(cacheProperties.getPetriNetNewest()).get("processDeleteTest") == null
        ImportPetriNetEventOutcome testNetOptional = petriNetService.importProcess(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId)
        assert testNetOptional.getProcess() != null
        Process testNet = testNetOptional.getProcess()

        assert cacheManager.getCache(cacheProperties.getPetriNetNewest()).get(testNet.getIdentifier()) == null
        Process test = petriNetService.getNewestVersionByIdentifier(testNet.getIdentifier())
        assert cacheManager.getCache(cacheProperties.getPetriNetNewest()).get(testNet.getIdentifier()).get().equals(test)
    }
}
