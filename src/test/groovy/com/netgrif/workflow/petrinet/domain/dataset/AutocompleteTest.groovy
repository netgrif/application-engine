package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class AutocompleteTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IDataService dataService

    @Autowired
    private SuperCreator superCreator

    @Value("classpath:petriNets/autocomplete.xml")
    private Resource netResource

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void test() {
        def user = superCreator.getLoggedSuper()
        def testNet = petriNetService.importPetriNet(netResource.inputStream, VersionType.MAJOR, user)

        assert testNet.isPresent()

        def aCase = workflowService.createCase(testNet.get().stringId, "Test", "", user)
        def task = taskService.findById(aCase.tasks.first().task)

        assert task != null
        def fields = dataService.getData(task, aCase)

        assert fields.find { it.getImportId() == "name" }.autocomplete == Autocomplete.GIVEN_NAME
        assert fields.find { it.getImportId() == "name2" }.autocomplete == null
        assert fields.find { it.getImportId() == "surname" }.autocomplete == Autocomplete.FAMILY_NAME
        assert fields.find { it.getImportId() == "address" }.autocomplete == Autocomplete.STREET_ADDRESS
    }
}
