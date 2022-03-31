package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.Importer
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
    private TestHelper testHelper

    @Value("classpath:net_import_1.xml")
    private Resource firstVersionResource
    @Value("classpath:net_import_2.xml")
    private Resource secondVersionResource

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void importTest() {
        int beforeImportNet = processRoleRepository.count()
        def netOptional = petriNetService.importPetriNet(
                firstVersionResource.inputStream,
                VersionType.MAJOR,
                superCreator.loggedSuper
        )
        assert netOptional.getNet() != null
        assert processRoleRepository.count() == beforeImportNet + 2
        int statusImportRole = processRoleRepository.count()
        def net = netOptional.getNet()

        // ASSERT IMPORTED NET
        assert net.importId == "new_model"
        assert net.version.major == 1
        assert net.version.minor == 0
        assert net.version.patch == 0
        assert net.initials == "NEW"
        assert net.title.defaultValue == "New Model"
        assert net.icon == "home"
        assert net.roles.size() == 2
        2.times {
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newRole_${it + 1}" as String)
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newRole_${it + 1}" as String)
        }
        assert net.dataSet.size() == 5
        5.times {
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 1}" as String)
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 1}" as String)
        }
        assert net.transitions.size() == 2
        2.times {
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("task${it + 1}" as String)
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].title.defaultValue == ("task${it + 1}" as String)
        }
        assert net.places.size() == 0

        // ASSERT IMPORTED NET FROM REPO
        net = petriNetService.getNewestVersionByIdentifier("new_model")
        assert net != null
        assert net.importId == "new_model"
        assert net.version.major == 1
        assert net.version.minor == 0
        assert net.version.patch == 0
        assert net.initials == "NEW"
        assert net.title.defaultValue == "New Model"
        assert net.icon == "home"
        assert net.roles.size() == 2
        2.times {
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newRole_${it + 1}" as String)
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newRole_${it + 1}" as String)
        }
        assert net.dataSet.size() == 5
        5.times {
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 1}" as String)
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 1}" as String)
        }
        assert net.transitions.size() == 2
        2.times {
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("task${it + 1}" as String)
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].title.defaultValue == ("task${it + 1}" as String)
        }
        assert net.places.size() == 0

        def netOptional2 = petriNetService.importPetriNet(
                secondVersionResource.inputStream,
                VersionType.MAJOR,
                superCreator.loggedSuper
        )

        assert processRoleRepository.count() == statusImportRole + 1
        assert netOptional2.getNet() != null
        def net2 = netOptional2.getNet()

        // ASSERT NEW IMPORTED NET
        assert net2.importId == "new_model"
        assert net2.version.major == 2
        assert net2.version.minor == 0
        assert net2.version.patch == 0
        assert net2.initials == "NEW"
        assert net2.title.defaultValue == "New Model2"
        assert net2.icon == "home2"
        assert net2.roles.size() == 1
        assert net2.roles.values()[0].importId == "newRole_3"
        assert net2.roles.values()[0].name.defaultValue == "newRole_3"
        assert net2.dataSet.size() == 2
        2.times {
            assert net2.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 6}" as String)
            assert net2.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 6}" as String)
        }
        assert net2.transitions.size() == 1
        net2.transitions.values()[0].importId == "task3"
        net2.transitions.values()[0].title.defaultValue == "task3"
        assert net2.places.size() == 0

        // ASSERT NEW NET FROM REPO
        net2 = petriNetService.getNewestVersionByIdentifier("new_model")
        assert net2 != null
        assert net2.importId == "new_model"
        assert net2.version.major == 2
        assert net2.version.minor == 0
        assert net2.version.patch == 0
        assert net2.initials == "NEW"
        assert net2.title.defaultValue == "New Model2"
        assert net2.icon == "home2"
        assert net2.roles.size() == 1
        assert net2.roles.values()[0].importId == "newRole_3"
        assert net2.roles.values()[0].name.defaultValue == "newRole_3"
        assert net2.dataSet.size() == 2
        2.times {
            assert net2.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 6}" as String)
            assert net2.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 6}" as String)
        }
        assert net2.transitions.size() == 1
        net2.transitions.values()[0].importId == "task3"
        net2.transitions.values()[0].title.defaultValue == "task3"
        assert net2.places.size() == 0

        // ASSERT OLD NET FROM REPO
        net = petriNetService.getPetriNet(net.stringId)
        assert net != null
        assert net.importId == "new_model"
        assert net.version.major == 1
        assert net.version.minor == 0
        assert net.version.patch == 0
        assert net.initials == "NEW"
        assert net.title.defaultValue == "New Model"
        assert net.icon == "home"
        assert net.roles.size() == 2
        2.times {
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newRole_${it + 1}" as String)
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newRole_${it + 1}" as String)
        }
        assert net.dataSet.size() == 5
        5.times {
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 1}" as String)
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 1}" as String)
        }
        assert net.transitions.size() == 2
        2.times {
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("task${it + 1}" as String)
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].title.defaultValue == ("task${it + 1}" as String)
        }
        assert net.places.size() == 0
    }

    @Test
    void createTransitionNoLabel(){
        PetriNet net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/NoLabel.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert net
        PetriNet importNet = petriNetService.findByImportId(net.getImportId()).get()
        assert importNet
        assert importNet.getTransition("1").getTitle()
        assert importNet.getTransition("layout").getTitle()
        assert importNet.getTransition("layout").getTitle().equals("")

    }

}
