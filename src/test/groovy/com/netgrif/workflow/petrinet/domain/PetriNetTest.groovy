package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.arcs.Arc
import com.netgrif.workflow.petrinet.domain.arcs.InhibitorArc
import com.netgrif.workflow.petrinet.domain.arcs.ReadArc
import com.netgrif.workflow.petrinet.domain.arcs.ResetArc
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PetriNetTest {

    public static final String CLONE_NET_FILE = "net_clone.xml"
    public static final String CLONE_NET_TITLE = "Clone"
    public static final String CLONE_NET_INITS = "CLN"
    public static final String CLONE_NET_TASK = "2"

    @Autowired
    private Importer importer

    private def stream = { String name ->
        return PetriNetTest.getClassLoader().getResourceAsStream(name)
    }

    @Test
    void testCloen() {
        def net = importer.importPetriNet(stream(CLONE_NET_FILE), CLONE_NET_TITLE, CLONE_NET_INITS).get()

        def clone = net.clone()

        def arcs = clone.getArcsOfTransition(CLONE_NET_TASK)

        assert arcs.size() == 4
        assert arcs.any { it instanceof Arc }
        assert arcs.any { it instanceof InhibitorArc }
        assert arcs.any { it instanceof ResetArc }
        assert arcs.any { it instanceof ReadArc }
    }
}