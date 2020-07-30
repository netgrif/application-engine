package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
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
class MapFieldTest {

    @Autowired
    private IPetriNetService petriNetService
    @Autowired
    private SuperCreator superCreator
    @Autowired
    private TestHelper testHelper

    @Value("classpath:data_map.xml")
    private Resource netResource

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testImport() {
        def netOptional = petriNetService.importPetriNet(netResource.inputStream, "major", superCreator.loggedSuper)
        assert netOptional.isPresent()

        def net = netOptional.get()
        assert net.dataSet.size() == 1

        EnumerationMapField field = net.dataSet["enumeration"] as EnumerationMapField
        assert field.name.defaultValue == "Enumeration map"
        assert field.choices.size() == 3
        assert field.choices["first"].defaultValue == "First option"
        assert field.choices["second"].defaultValue == "Second option"
        assert field.choices["third"].defaultValue == "Third option"
        assert field.defaultValue == "second"
    }
}