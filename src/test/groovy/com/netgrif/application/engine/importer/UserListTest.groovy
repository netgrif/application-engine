package com.netgrif.application.engine.importer

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.workflow.domain.VersionType
import com.netgrif.application.engine.workflow.domain.dataset.Field
import com.netgrif.application.engine.workflow.domain.dataset.UserFieldValue
import com.netgrif.application.engine.workflow.domain.dataset.UserListField
import com.netgrif.application.engine.workflow.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.workflow.domain.throwable.MissingProcessMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportProcessEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.UseCaseRepository
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
class UserListTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private UseCaseRepository caseRepository;

    @Autowired
    private IDataService dataService;

    @Autowired
    private ITaskService taskService;

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testUserList() throws MissingProcessMetaDataException, IOException {
        ImportProcessEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/user_list.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());

        assert net.getNet() != null;
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq("User List"));

        assert caseOpt.isPresent();
        assert caseOpt.get().getDataSet().get("text").getRawValue() == "Its working...";

        Task task = taskService.findByCases(new FullPageRequest(), Collections.singletonList(caseOpt.get().getStringId())).stream().collect(Collectors.toList()).get(0)

        dataService.setData(task.stringId, new DataSet([
                "users_1": new UserListField(rawValue: new UserListFieldValue(new UserFieldValue(superCreator.getSuperUser())))
        ] as Map<String, Field<?>>), superCreator.getLoggedSuper())

//        TODO: release/8.0.0
//        assert taskService.findById(task.stringId).users.get(superCreator.getSuperUser().getStringId())
//        assert caseRepository.findById(caseOpt.get().stringId).get().users.get(superCreator.getSuperUser().getStringId())
    }
}
