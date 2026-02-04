package com.netgrif.application.engine.importer

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.GroupService
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase
import com.netgrif.application.engine.objects.workflow.domain.Task
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.util.stream.Collectors

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ActorListTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private IDataService dataService

    @Autowired
    private GroupService groupService

    @Autowired
    private ITaskService taskService

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testUserList() throws MissingPetriNetMetaDataException, IOException {
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/actor_list.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build());

        assert net.getNet() != null
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq("Actor List"))

        assert caseOpt.isPresent()
        assert caseOpt.get().getDataSet().get("text").getValue() == "Its working..."

        Task task = taskService.findByCases(new FullPageRequest(), Collections.singletonList(caseOpt.get().getStringId())).stream().collect(Collectors.toList()).get(0)

        dataService.setData(task.stringId, ImportHelper.populateDataset([
                "users_1": [
                        "value": [superCreator.getSuperUser().getStringId()],
                        "type" : "actorList"
                ]
        ]))

        assert taskService.findById(task.stringId).actors.get(superCreator.getSuperUser().getStringId())
        assert caseRepository.findById(caseOpt.get().stringId).get().actors.get(superCreator.getSuperUser().getStringId())

        dataService.setData(task.stringId, ImportHelper.populateDataset([
                "users_1": [
                        "value": [groupService.getDefaultSystemGroup().getStringId()],
                        "type" : "actorList"
                ]
        ]))

        assert taskService.findById(task.stringId).actors.get(groupService.getDefaultSystemGroup().getStringId())
        assert caseRepository.findById(caseOpt.get().stringId).get().actors.get(groupService.getDefaultSystemGroup().getStringId())
    }
}
