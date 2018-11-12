package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.arcs.ArcOrderComparator
import com.netgrif.workflow.petrinet.domain.arcs.ResetArc
import com.netgrif.workflow.startup.ImportHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ArcOrderTest {

    public static final String NET_FILE = "arc_order_test.xml"
    public static final String NET_TITLE = "ArcOrder"
    public static final String NET_INITS = "ACR"
    public static final String NET_TASK = "1"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper helper

    private def stream = { String name ->
        return ArcOrderTest.getClassLoader().getResourceAsStream(name)
    }

    @Test
    void  testOrder() {
        def net = importer.importPetriNet(stream(NET_FILE), NET_TITLE, NET_INITS).get()

        def arcs = net.getArcsOfTransition(NET_TASK)
        def sorted = arcs.sort { a1, a2 -> ArcOrderComparator.getInstance().compare(a1,a2) }
        assert sorted.last() instanceof ResetArc

        def instance = helper.createCase("Arc Case", net)
        helper.assignTaskToSuper(NET_TASK, instance.stringId)

        assert true
    }
}