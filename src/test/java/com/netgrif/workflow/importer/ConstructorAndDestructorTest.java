package com.netgrif.workflow.importer;

import com.netgrif.workflow.TestHelper;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.SuperCreator;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.QCase;
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class ConstructorAndDestructorTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private CaseRepository caseRepository;

    @Before
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void testConstructorAndDestructor() throws MissingPetriNetMetaDataException, IOException {
        Optional<PetriNet> net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/constructor_destructor.xml"), "major", superCreator.getLoggedSuper());

        assert net.isPresent();
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq("Construct"));

        assert caseOpt.isPresent();
        assert caseOpt.get().getDataSet().get("text").getValue() == "Its working...";
    }
}
