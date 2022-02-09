package com.netgrif.application.engine.importer

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
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
class UserListTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private IDataService dataService;

    @Autowired
    private ITaskService taskService;

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testUserList() throws MissingPetriNetMetaDataException, IOException {
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/user_list.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());

        assert net.getNet() != null;
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq("User List"));

        assert caseOpt.isPresent();
        assert caseOpt.get().getDataSet().get("text").getValue() == "Its working...";

        Task task = taskService.findByCases(new FullPageRequest(), Collections.singletonList(caseOpt.get().getStringId())).stream().collect(Collectors.toList()).get(0)

        dataService.setData(task.stringId, ImportHelper.populateDataset([
                "users_1": [
                        "value": [superCreator.getSuperUser().getStringId()],
                        "type" : "userList"
                ]
        ]))

        assert taskService.findById(task.stringId).users.get(superCreator.getSuperUser().getStringId())
        assert caseRepository.findById(caseOpt.get().stringId).get().users.get(superCreator.getSuperUser().getStringId())
    }
}
