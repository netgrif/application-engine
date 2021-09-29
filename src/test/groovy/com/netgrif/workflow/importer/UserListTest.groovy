package com.netgrif.workflow.importer

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.utils.FullPageRequest
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.QCase
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
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
        Optional<PetriNet> net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/user_list.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert net.isPresent();
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq("User List"))

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
