package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.arcs.Arc
import com.netgrif.application.engine.petrinet.domain.arcs.InhibitorArc
import com.netgrif.application.engine.petrinet.domain.arcs.ReadArc
import com.netgrif.application.engine.petrinet.domain.arcs.ResetArc
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
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
    private TestHelper testHelper

    @Value("classpath:net_clone.xml")
    private Resource netResource

    @Value("classpath:net_import_1.xml")
    private Resource netResource2

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testClone() {
        int beforeImportNet = processRoleRepository.count()

        def netOptional = petriNetService.importPetriNet(netResource.inputStream, VersionType.MAJOR, superCreator.loggedSuper)

        assert netOptional.getNet() != null

        def net = netOptional.getNet()
        def clone = net.clone()

        def arcs = clone.getArcsOfTransition(CLONE_NET_TASK)

        assert arcs.size() == 4
        assert arcs.any { it instanceof Arc }
        assert arcs.any { it instanceof InhibitorArc }
        assert arcs.any { it instanceof ResetArc }
        assert arcs.any { it instanceof ReadArc }

        assert net.roles.size() == 2
        assert processRoleRepository.count() == beforeImportNet + 2
    }

    @Test
    void testVersioning() {
        def netOptional = petriNetService.importPetriNet(netResource.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        assert netOptional.getNet() != null

        def netOptional2 = petriNetService.importPetriNet(netResource.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        assert netOptional2.getNet() != null

        def netOptional3 = petriNetService.importPetriNet(netResource2.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        assert netOptional3.getNet() != null

        def nets = petriNetService.getReferencesByVersion(null, superCreator.loggedSuper, Locale.UK)
        assert nets.findAll { it.identifier in [netOptional.getNet().identifier, netOptional3.getNet().identifier] }.size() == 2
        assert nets.find { it.identifier == "new_model" }.version == "1.0.0"
        assert nets.find { it.identifier == "test" }.version == "2.0.0"
    }
}