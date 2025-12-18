package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.arcs.Arc
import com.netgrif.application.engine.objects.petrinet.domain.arcs.InhibitorArc
import com.netgrif.application.engine.objects.petrinet.domain.arcs.ReadArc
import com.netgrif.application.engine.objects.petrinet.domain.arcs.ResetArc
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    private SuperCreatorRunner superCreator

    @Autowired
    private ProcessRoleRepository processRoleRepository

    @Autowired
    private TestHelper testHelper

    @Value("classpath:net_clone.xml")
    private Resource netResource

    @Value("classpath:net_import_1.xml")
    private Resource netResource2

    @Value("classpath:net_clone2.xml")
    private Resource netResource3

    @Value("classpath:net_clone3.xml")
    private Resource netResource4

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testClone() {
        int beforeImportNet = processRoleRepository.count()

        def netOptional = petriNetService.importPetriNet(netResource.inputStream, VersionType.MAJOR, superCreator.loggedSuper)

        assert netOptional.getNet() != null

        PetriNet net = netOptional.getNet()
        PetriNet clone = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet(net as com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet)

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

        Page<PetriNetReference> nets = petriNetService.getReferencesByVersion(null, superCreator.loggedSuper, Locale.UK, Pageable.unpaged())
        assert nets.findAll { it.identifier in [netOptional.getNet().identifier, netOptional3.getNet().identifier] }.size() == 2
        assert nets.find { it.identifier == "new_model" }.version == "1.0.0"
        assert nets.find { it.identifier == "test" }.version == "2.0.0"
    }

    @Test
    void testVersion() {
        def zeroImport = petriNetService.importPetriNet(netResource3.inputStream, VersionType.PATCH, superCreator.loggedSuper)
        assert zeroImport.getNet() != null
        assert zeroImport.getNet().version.toString() == "0.0.1"

        def firstImport = petriNetService.importPetriNet(netResource.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        assert firstImport.getNet() != null
        assert firstImport.getNet().version.toString() == "1.0.0"

        def secondImport = petriNetService.importPetriNet(netResource.inputStream, VersionType.MINOR, superCreator.loggedSuper)
        assert secondImport.getNet().version.toString() == "1.1.0"

        def thirdImport = petriNetService.importPetriNet(netResource.inputStream, VersionType.PATCH, superCreator.loggedSuper)
        assert thirdImport.getNet().version.toString() == "1.1.1"

        def lastImport = petriNetService.importPetriNet(netResource4.inputStream, VersionType.PATCH, superCreator.loggedSuper)
        assert lastImport.getNet().version.toString() == "3.1.1"

        Page<PetriNet> nets = petriNetService.getByIdentifier(zeroImport.getNet().identifier, Pageable.unpaged())
        assert nets.getSize() == 5
    }


    @Test
    void testVersioningConflicts() {
        def zeroImport = petriNetService.importPetriNet(netResource3.inputStream, VersionType.PATCH, superCreator.loggedSuper)
        assert zeroImport.getNet() != null
        assert zeroImport.getNet().version.toString() == "0.0.1"

        try {
            petriNetService.importPetriNet(netResource3.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        } catch (Exception e) {
            assert e.getMessage() == "A process [test] with such version [0.0.1] already exists"
        }
    }

}
