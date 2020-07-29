package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.SuperCreator
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
class ImporterTest {

    @Autowired
    private Importer importer
    @Autowired
    private IPetriNetService petriNetService
    @Autowired
    private SuperCreator superCreator
    @Autowired
    private ProcessRoleRepository processRoleRepository
    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository
    @Autowired
    private TestHelper testHelper

    @Value("classpath:net_import_1.xml")
    private Resource firstVersionResource
    @Value("classpath:net_import_2.xml")
    private Resource secondVersionResource

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void importTest() {
        importAndAssert(firstVersionResource.inputStream, "1.0.0", 2, 5, 2, 0)
        assert processRoleRepository.count() == 3
        assert userProcessRoleRepository.count() == 3

        importAndAssert(secondVersionResource.inputStream, "2.0.0",1, 3, 1, 0)
        assert processRoleRepository.count() == 4
        assert userProcessRoleRepository.count() == 4
    }

    private void importAndAssert(InputStream netStream, String version, int roles, int data, int transitions, int places) {
        def netOptional = petriNetService.importPetriNet(
                netStream,
                "major",
                superCreator.loggedSuper
        )
        assert netOptional.isPresent()
        def net = netOptional.get()

        assert net.roles.size() == roles
        assert net.dataSet.size() == data
        assert net.transitions.size() == transitions
        assert net.places.size() == places
        assert net.version == version
    }
}
