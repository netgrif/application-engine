package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
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
class StaticDataSetTest {

    private static final String FIELD_STATIC_TEXT = "static_text"
    private static final String FIELD_STATIC_FILE = "static_file"
    private static final String FIELD_REGULAR_TEXT = "regular_text"
    private static final String FIELD_REGULAR_FILE = "regular_file"

    private static final String FIELD_STATIC_TEXT_INIT = "static value"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private PetriNetRepository repository

    @Autowired
    private IDataService dataService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    PetriNet net

    @Before
    void beforeAll() {
        testHelper.truncateDbs()
        def netOpt = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/static_dataset_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netOpt.isPresent()
        net = netOpt.get()
    }

    @Test
    void testImport() {
        assert net.staticDataSet.containsKey(FIELD_STATIC_TEXT)
        assert net.staticDataSet.containsKey(FIELD_STATIC_FILE)
        assert net.dataSet.containsKey(FIELD_REGULAR_TEXT)
        assert net.dataSet.containsKey(FIELD_REGULAR_FILE)
        assert net.staticDataSet.get(FIELD_STATIC_TEXT).value == FIELD_STATIC_TEXT_INIT
    }

    @Test
    void testDataSetValueSave() {
        net.dataSet.get(FIELD_REGULAR_TEXT).value = "INVALID VALUE"
        net.staticDataSet.get(FIELD_STATIC_TEXT).value = "VALID VALUE"
        net = repository.save(net)
        assert net.dataSet.get(FIELD_REGULAR_TEXT).value == null
        assert net.staticDataSet.get(FIELD_STATIC_TEXT).value == "VALID VALUE"
    }

}
