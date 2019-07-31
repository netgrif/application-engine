package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.startup.DefaultRoleRunner
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
import com.netgrif.workflow.workflow.domain.EventOutcome
import com.netgrif.workflow.workflow.service.TaskService
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
class ActionRefTest {

    public static final String NET_FILE = "actionref_test.xml"
    public static final String NET_TITLE = "ActionRef"
    public static final String NET_INITS = "ACR"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper helper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskService taskService

    @Autowired
    private PetriNetRepository netRepository

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private DefaultRoleRunner roleRunner

    @Autowired
    private MongoTemplate template

    @Autowired
    private UserRepository userRepository

    @Autowired
    private UserProcessRoleRepository roleRepository

    private def stream = { String name ->
        return ActionRefTest.getClassLoader().getResourceAsStream(name)
    }

    Case instance
    EventOutcome outcome

    @Test
    void testEventImport() {
        template.db.drop()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        roleRunner.run()
        superCreator.run()

        def net = importer.importPetriNet(stream(NET_FILE), NET_TITLE, NET_INITS).get()

        assert net.dataSet.get("text_1").actions.size() == 8
        assert net.transitions.get("task").dataSet.get("text_1").actions.size() == 8
    }
}