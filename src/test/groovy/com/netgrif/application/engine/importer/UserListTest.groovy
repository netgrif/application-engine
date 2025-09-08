package com.netgrif.application.engine.importer

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.authorization.domain.User
import com.netgrif.application.engine.authorization.service.interfaces.IUserService
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.petrinet.domain.params.ImportProcessParams
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
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
class UserListTest {

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
    private IUserService userService

    @Autowired
    private IRoleService roleService

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testUserList() throws MissingPetriNetMetaDataException, IOException {
        TestHelper.login(superCreator.superIdentity)
        ImportPetriNetEventOutcome net = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/user_list.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId()))

        assert net.getProcess() != null;
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq("User List"));

        assert caseOpt.isPresent();
        assert caseOpt.get().getDataSet().get("text").getRawValue() == "Its working...";

        Task task = taskService.findByCases(new FullPageRequest(), Collections.singletonList(caseOpt.get().getStringId())).stream().collect(Collectors.toList()).get(0)

        User user = userService.findById(superCreator.getLoggedSuper().activeActorId).get()
        dataService.setData(new SetDataParams(task.stringId, new DataSet([
                "users_1": new UserListField(rawValue: new UserListFieldValue(new UserFieldValue(user)))
        ] as LinkedHashMap<String, Field<?>>), superCreator.getLoggedSuper().getActiveActorId()))

        Role caseRole = roleService.findCaseRoleByCaseIdAndImportId(caseOpt.get().stringId, "users_1")
        assert taskService.findById(task.stringId).caseRolePermissions.containsKey(caseRole.stringId)
        assert caseRepository.findById(caseOpt.get().stringId).get().caseRolePermissions.containsKey(caseRole.stringId)
    }
}
