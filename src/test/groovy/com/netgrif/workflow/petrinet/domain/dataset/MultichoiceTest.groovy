package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.ipc.TaskApiTest
import com.netgrif.workflow.startup.ImportHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class MultichoiceTest {

    public static final String MULTICHOICE_NET_FILE = "case_multichoice_test.xml"
    public static final String NET_TITLE = "CMT"
    public static final String NET_INITIALS = "CMT"
    public static final String NET_TASK_EDIT_COST = "Tran"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper helper

    @Autowired
    private TestHelper testHelper

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @Before
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void testMultichoiceField() {
        def netOptional = importer.importPetriNet(stream(MULTICHOICE_NET_FILE), NET_TITLE, NET_INITIALS)
        assert netOptional.isPresent()
        def net = netOptional.get()
    }

}
