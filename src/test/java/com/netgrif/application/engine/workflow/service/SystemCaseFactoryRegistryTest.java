package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class SystemCaseFactoryRegistryTest {

    @Autowired
    private SystemCaseFactoryRegistry registry;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private IUserService userService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper importHelper;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void testFromCase() throws IOException, MissingPetriNetMetaDataException {
        assert registry.fromCase(null) == null;

        testHelper.login(superCreator.getSuperIdentity());

        Process testProcess = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"),
                VersionType.MAJOR, superCreator.getSuperIdentity().getMainActorId()).getNet();
        Case testCase = importHelper.createCase("Test", testProcess);

        assert registry.fromCase(workflowService.findOne(testCase.getStringId())) == null;

        User user = userService.create(UserParams.with()
                .email(new TextField("s@meemail.com"))
                .build());

        SystemCase systemCase = registry.fromCase(user.getCase());
        assert systemCase != null;
        assert systemCase instanceof User;
        assert systemCase.getCase().getStringId().equals(user.getStringId());

        // todo 2058 group case
    }
}
