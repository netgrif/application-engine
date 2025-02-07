package com.netgrif.application.engine.importer;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.core.petrinet.domain.VersionType;
import com.netgrif.core.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.adapter.petrinet.service.PetriNetService;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.core.workflow.domain.QCase;
import com.netgrif.core.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ConstructorAndDestructorTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private PetriNetService petriNetService;

    @Autowired
    private SuperCreatorRunner superCreator;

    @Autowired
    private CaseRepository caseRepository;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void testConstructorAndDestructor() throws MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        ImportPetriNetEventOutcome outcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/constructor_destructor.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());

        assert outcome.getNet() != null;
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq("Construct"));

        assert caseOpt.isPresent();
        assert "Its working...".equals(caseOpt.get().getDataSet().get("text").getValue());
    }
}
