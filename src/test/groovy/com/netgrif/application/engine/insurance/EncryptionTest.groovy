package com.netgrif.application.engine.insurance

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.LoggedUser
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static com.netgrif.application.engine.petrinet.domain.VersionType.MAJOR

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class EncryptionTest extends EngineTest {

    private final String FIELD_ID = "2"
    private final String FIELD_VALUE = "Bratislava"

    @Test
    void testEncryption() {
        String id = createCase()

        Case useCase = loadCase(id)

        assertCorrectEncrypting(useCase)
    }

    private void assertCorrectEncrypting(Case useCase) {
        TextField field = useCase.dataSet.get(FIELD_ID) as TextField
        assert field.rawValue == FIELD_VALUE

        def rawCaseOpt = caseRepository.findById(useCase.stringId)

        assert rawCaseOpt.isPresent()
        TextField rawField = rawCaseOpt.get().dataSet.get(FIELD_ID) as TextField
        assert rawField.rawValue != FIELD_VALUE
    }

    private Case loadCase(String id) {
        Case useCase = workflowService.findOne(id)
        assert useCase != null
        return useCase
    }

    private String createCase() {
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/mapping_test.xml"), MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        return workflowService.createCase(net.getNet().stringId, "Encryption test", "color", mockLoggedUser()).getCase()
    }

    LoggedUser mockLoggedUser() {
        def authorityUser = authorityService.getOrCreate(Authority.user)
        return new LoggedUser(superCreator.getSuperUser().getStringId(), superAdminConfiguration.email, superAdminConfiguration.password, [authorityUser])
    }
}