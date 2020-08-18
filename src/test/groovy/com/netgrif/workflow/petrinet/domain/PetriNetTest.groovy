package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.arcs.Arc
import com.netgrif.workflow.petrinet.domain.arcs.InhibitorArc
import com.netgrif.workflow.petrinet.domain.arcs.ReadArc
import com.netgrif.workflow.petrinet.domain.arcs.ResetArc
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
class PetriNetTest {

    public static final String CLONE_NET_TASK = "2"

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

    @Value("classpath:net_clone.xml")
    private Resource netResource

    @Value("classpath:net_import_1.xml")
    private Resource netResource2

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testClone() {
        def netOptional = petriNetService.importPetriNet(netResource.inputStream, "major", superCreator.loggedSuper)

        assert netOptional.isPresent()

        def net = netOptional.get()
        def clone = net.clone()

        def arcs = clone.getArcsOfTransition(CLONE_NET_TASK)

        assert arcs.size() == 4
        assert arcs.any { it instanceof Arc }
        assert arcs.any { it instanceof InhibitorArc }
        assert arcs.any { it instanceof ResetArc }
        assert arcs.any { it instanceof ReadArc }

        assert net.roles.size() == 2
        assert processRoleRepository.count() == 3
        assert userProcessRoleRepository.count() == 3
    }

    @Test
    void testVersioning() {
        def netOptional = petriNetService.importPetriNet(netResource.inputStream, "major", superCreator.loggedSuper)
        assert netOptional.isPresent()

        def netOptional2 = petriNetService.importPetriNet(netResource.inputStream, "major", superCreator.loggedSuper)
        assert netOptional2.isPresent()

        def netOptional3 = petriNetService.importPetriNet(netResource2.inputStream, "major", superCreator.loggedSuper)
        assert netOptional3.isPresent()

        def nets = petriNetService.getReferencesByVersion(null, superCreator.loggedSuper, Locale.UK)
        assert nets.size() == 2
        assert nets.find { it.identifier == "new_model" }.version == "1.0.0"
        assert nets.find { it.identifier == "test" }.version == "2.0.0"
    }
}