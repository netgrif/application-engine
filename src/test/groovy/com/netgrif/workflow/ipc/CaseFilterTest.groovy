package com.netgrif.workflow.ipc

import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.auth.service.AuthorityService
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.startup.DefaultRoleRunner
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.startup.SystemUserRunner
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class CaseFilterTest {

    public static final String INSURANCE_NET_NAME = "Insurance"
    public static final String INSURANCE_NET_FILE = "ipc_where.xml"
    public static final String INSURANCE_NET_INITIALS = "INS"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private AuthorityService authorityService

    @Autowired
    private MongoTemplate template

    @Autowired
    private UserRepository userRepository

    @Autowired
    private UserProcessRoleRepository roleRepository

    @Autowired
    private SystemUserRunner systemUserRunner

    @Autowired
    private DefaultRoleRunner roleRunner

    @Autowired
    private CaseRepository caseRepository

    private Optional<PetriNet> testNet

    @Autowired
    private Importer importer

    private def stream = { String name ->
        return CaseFilterTest.getClassLoader().getResourceAsStream(name)
    }

    @Test
    void testFilter() {
        template.db.dropDatabase()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        roleRunner.run()
        superCreator.run()
        systemUserRunner.run()

        testNet = importer.importPetriNet(stream(INSURANCE_NET_FILE), INSURANCE_NET_NAME, INSURANCE_NET_INITIALS)

        assert testNet.isPresent()

        List<Case> cases = []
        5.times { index ->
            cases << importHelper.createCase("Case $index" as String, testNet.get())
        }

        importHelper.assignTaskToSuper("Task", cases[0].stringId)
        importHelper.finishTaskAsSuper("Task", cases[0].stringId)

        cases = caseRepository.findAll()
        assert cases.find { it.title == "Case 1" }.dataSet["field"].value != 0
        assert cases.findAll { it.title != "Case 1" }.every { it.dataSet["field"].value == 0 }
    }
}