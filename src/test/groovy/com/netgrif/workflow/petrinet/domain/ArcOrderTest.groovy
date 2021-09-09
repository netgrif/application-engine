package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.arcs.ArcOrderComparator
import com.netgrif.workflow.petrinet.domain.arcs.ResetArc
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ArcOrderTest {

    public static final String NET_FILE = "arc_order_test.xml"
    public static final String NET_TASK = "1"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper helper

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    private def stream = { String name ->
        return ArcOrderTest.getClassLoader().getResourceAsStream(name)
    }


    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testOrder() {
        def net = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).get()

        def arcs = net.getArcsOfTransition(NET_TASK)
        def sorted = arcs.sort { a1, a2 -> ArcOrderComparator.getInstance().compare(a1, a2) }
        assert sorted.last() instanceof ResetArc

        def instance = helper.createCase("Arc Case", net)
        helper.assignTaskToSuper(NET_TASK, instance.stringId)

        assert true
    }
}