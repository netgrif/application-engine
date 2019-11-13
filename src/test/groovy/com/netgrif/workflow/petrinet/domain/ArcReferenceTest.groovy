package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.startup.ImportHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SuppressWarnings("GroovyAssignabilityCheck")
@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ArcReferenceTest {

    public static final String NET_FILE = "arc_order_test.xml"
    public static final String NET_TITLE = "ArcOrder"
    public static final String NET_INITS = "ACR"
    public static final String NET_INVALID_FILE = "arc_reference_invalid_test.xml"
    public static final String NET_INVALID_TITLE = "ArcOrder"
    public static final String NET_INVALID_INITS = "ACR"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper helper

    private def stream = { String name ->
        return ArcOrderTest.getClassLoader().getResourceAsStream(name)
    }

    @Test
    void testReference() {
        def net = importer.importPetriNet(stream(NET_FILE), NET_TITLE, NET_INITS).get()

        assert net
    }

    @Test
    void testInvalidReference() {
        try {
            importer.importPetriNet(stream(NET_INVALID_FILE), NET_INVALID_TITLE, NET_INVALID_INITS).get()
            assert false
        } catch(IllegalArgumentException ignored) {
            assert true
        }
    }
}