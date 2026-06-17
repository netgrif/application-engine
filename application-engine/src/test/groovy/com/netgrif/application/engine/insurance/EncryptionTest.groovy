package com.netgrif.application.engine.insurance

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.LoggedUser
import com.netgrif.application.engine.auth.service.AuthorityService
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.DataField
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.TaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class EncryptionTest {

    @Autowired
    private TaskService taskService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private Importer importer

    @Autowired
    private AuthorityService authorityService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    private final String FIELD_NAME = "City"
    private final String FIELD_VALUE = "Bratislava"

    @Test
    void testEncryption() {
        String id = createCase()

        Case useCase = loadCase(id)

        assertCorrectEncrypting(useCase)
    }

    private void assertCorrectEncrypting(Case useCase) {
        def nameField = useCase.petriNet.dataSet.values().find { v -> v.name == FIELD_NAME }
        DataField field = useCase.dataSet.get(nameField.stringId)
        assert field.value == FIELD_VALUE

        def rawCaseOpt = caseRepository.findById(useCase.stringId)

        assert rawCaseOpt.isPresent()

        DataField rawField = rawCaseOpt.get().dataSet.get(nameField.stringId)
        assert rawField.value != FIELD_VALUE
    }

    private Case loadCase(String id) {
        Case useCase = workflowService.findOne(id)
        assert useCase != null
        return useCase
    }

    private String createCase() {
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/mapping_test.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        assert net.getNet() != null
        def useCase = workflowService.createCase(CreateCaseParams.with()
                .process(net.getNet())
                .title("Encryption test")
                .color("color")
                .author(mockLoggedUser())
                .build()).getCase()
        def nameField = useCase.petriNet.dataSet.values().find { v -> v.name.defaultValue == FIELD_NAME }
        useCase.dataSet.put(nameField.stringId, new DataField(FIELD_VALUE))
        return workflowService.save(useCase).stringId
    }

    LoggedUser mockLoggedUser() {
        def authorityUser = authorityService.getOrCreate(Authority.user)
        def superUser = superCreator.getSuperUser();
        LoggedUser loggedUser = new LoggedUserImpl(superUser.stringId, superUser.realmId, superUser.username, superUser.firstName, superUser.middleName, superUser.lastName, superUser.email, superUser.avatar, null, null, null, null);
        loggedUser.setAuthoritySet([authorityUser] as Set)
        return loggedUser
    }
}
