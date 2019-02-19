package com.netgrif.workflow.insurance

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService
import com.netgrif.workflow.importer.service.Config
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.DataField
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.TaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
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
    private IAuthorityService authorityService

    private final String FIELD_NAME = "City"
    private final String FIELD_VALUE = "Bratislava"

    @Test
    void testEncryption() {
        String id = createCase()

        Case useCase = loadCase(id)

        assertCorrectEncrypting(useCase)
    }

    private void assertCorrectEncrypting(Case useCase) {
        def nameField = useCase.petriNet.dataSet.values().find { v -> v.name == FIELD_NAME}
        DataField field = useCase.dataSet.get(nameField.stringId)
        assert field.value == FIELD_VALUE

        def rawCase = caseRepository.findOne(useCase.stringId)
        DataField rawField = rawCase.dataSet.get(nameField.stringId)
        assert rawField.value != FIELD_VALUE
    }

    private Case loadCase(String id) {
        Case useCase = workflowService.findOne(id)
        assert useCase != null
        return useCase
    }

    private String createCase() {
        Optional<PetriNet> net = importer.importPetriNet(new File("src/test/resources/mapping_test.xml"), "Encryption test", "ENC", new Config())
        assert net.isPresent()
        def useCase = workflowService.createCase(net.get().stringId, "Encryption test", "color", mockLoggedUser())
        def nameField = useCase.petriNet.dataSet.values().find { v -> v.name.defaultValue == FIELD_NAME}
        useCase.dataSet.put(nameField.stringId, new DataField(FIELD_VALUE))
        return workflowService.save(useCase).stringId
    }

    LoggedUser mockLoggedUser(){
        def authorityUser = authorityService.getOrCreate(Authority.user)
        return new LoggedUser(1L, "super@netgrif.com","password", [authorityUser])
    }
}