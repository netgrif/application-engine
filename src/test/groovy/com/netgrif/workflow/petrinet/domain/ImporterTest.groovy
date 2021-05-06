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
        def netOptional = petriNetService.importPetriNet(
                firstVersionResource.inputStream,
                "major",
                superCreator.loggedSuper
        )
        assert processRoleRepository.count() == 3
        assert userProcessRoleRepository.count() == 3
        assert netOptional.isPresent()
        def net = netOptional.get()

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
                "major",
                superCreator.loggedSuper
        )
        assert processRoleRepository.count() == 4
        assert userProcessRoleRepository.count() == 4
        assert netOptional2.isPresent()
        def net2 = netOptional2.get()

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
}
