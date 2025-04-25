package com.netgrif.application.engine.importer

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authorization.domain.Actor
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.authorization.service.interfaces.IActorService
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.ActorFieldValue
import com.netgrif.application.engine.petrinet.domain.dataset.ActorListField
import com.netgrif.application.engine.petrinet.domain.dataset.ActorListFieldValue
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
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
@CompileStatic
class ActorListTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private IDataService dataService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IActorService actorService

    @Autowired
    private IRoleService roleService

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testActorList() throws MissingPetriNetMetaDataException, IOException {
        testHelper.login(superCreator.superIdentity)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/actor_list.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId());

        assert net.getNet() != null;
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq("Actor List"));

        assert caseOpt.isPresent();
        assert caseOpt.get().getDataSet().get("text").getRawValue() == "Its working...";

        Task task = taskService.findByCases(new FullPageRequest(), Collections.singletonList(caseOpt.get().getStringId())).stream().collect(Collectors.toList()).get(0)

        Actor actor = actorService.findById(superCreator.getLoggedSuper().activeActorId).get()
        dataService.setData(task.stringId, new DataSet([
                "actors_1": new ActorListField(rawValue: new ActorListFieldValue(new ActorFieldValue(actor)))
        ] as Map<String, Field<?>>), superCreator.getLoggedSuper().getActiveActorId())

        Role caseRole = roleService.findCaseRoleByCaseIdAndImportId(caseOpt.get().stringId, "actors_1")
        assert taskService.findById(task.stringId).caseRolePermissions.containsKey(caseRole.stringId)
        assert caseRepository.findById(caseOpt.get().stringId).get().caseRolePermissions.containsKey(caseRole.stringId)
    }
}
