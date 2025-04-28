package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.*;
import com.netgrif.application.engine.authorization.domain.repositories.CaseRoleRepository;
import com.netgrif.application.engine.authorization.domain.repositories.ProcessRoleRepository;
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.netgrif.application.engine.petrinet.domain.Process;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class CaseAuthorizationServiceTest {

    @Autowired
    private CaseAuthorizationService authorizationService;

    @Autowired
    private IIdentityService identityService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private CaseRoleRepository caseRoleRepository;

    @Autowired
    private ApplicationRoleRunner applicationRoleRunner;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private TestHelper testHelper;

    private Identity testIdentity;

    private Process testProcess;

    private Process testProcessWithDefault;

    @BeforeEach
    public void beforeEach() throws IOException, MissingPetriNetMetaDataException {
        testHelper.truncateDbs();

        testIdentity = identityService.createWithDefaultActor(IdentityParams.with()
                        .username(new TextField("username"))
                        .password(new TextField("password"))
                        .firstname(new TextField("firstname"))
                        .lastname(new TextField("lastname"))
                .build());

        testProcess = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/case_authorization_service_test.xml"),
                VersionType.MAJOR, identityService.getLoggedSystemIdentity().getActiveActorId()).getNet();

        testProcessWithDefault = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/case_authorization_default_service_test.xml"),
                VersionType.MAJOR, identityService.getLoggedSystemIdentity().getActiveActorId()).getNet();

        testHelper.login(testIdentity);
    }

    @Test
    public void canCallCreate() {
        // order of assertions is important!
        assert !authorizationService.canCallCreate(null);
        assert authorizationService.canCallCreate(testProcessWithDefault.getStringId());
        assert !authorizationService.canCallCreate(testProcess.getStringId());

        assignProcessRole(processRoleRepository.findByImportId("pos_process_role"));
        assert authorizationService.canCallCreate(testProcess.getStringId());

        assignProcessRole(processRoleRepository.findByImportId("neg_process_role"));
        assert !authorizationService.canCallCreate(testProcess.getStringId());

        assignAppRole(applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));
        assert authorizationService.canCallCreate(testProcess.getStringId());

        testHelper.logout();
        assert !authorizationService.canCallCreate(testProcess.getStringId());
    }

    @Test
    public void canCallDelete() {
        // order of assertions is important!
        Case testCase = importHelper.createCase("test", testProcess);
        Case testCaseWithDefault = importHelper.createCase("test with default role", testProcessWithDefault);

        assert !authorizationService.canCallDelete(testCase.getStringId());
        assert authorizationService.canCallDelete(testCaseWithDefault.getStringId());

        assignProcessRole(processRoleRepository.findByImportId("pos_process_role"));
        assert authorizationService.canCallDelete(testCase.getStringId());

        assignProcessRole(processRoleRepository.findByImportId("neg_process_role"));
        assert !authorizationService.canCallDelete(testCase.getStringId());

        assignCaseRole(caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role"),
                testCase);
        assert authorizationService.canCallDelete(testCase.getStringId());

        assignCaseRole(caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role"),
                testCase);
        assert !authorizationService.canCallDelete(testCase.getStringId());

        assignAppRole(applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));
        assert authorizationService.canCallDelete(testCase.getStringId());

        testHelper.logout();
        assert !authorizationService.canCallDelete(testCase.getStringId());
    }

    private void assignProcessRole(ProcessRole role) {
        RoleAssignment assignment = new ProcessRoleAssignment();
        assignment.setActorId(testIdentity.getMainActorId());
        assignment.setRoleId(role.getStringId());
        roleAssignmentRepository.save(assignment);
    }

    private void assignCaseRole(CaseRole role, Case testCase) {
        CaseRoleAssignment assignment = new CaseRoleAssignment();
        assignment.setActorId(testIdentity.getMainActorId());
        assignment.setRoleId(role.getStringId());
        assignment.setCaseId(testCase.getStringId());
        roleAssignmentRepository.save(assignment);
    }

    private void assignAppRole(ApplicationRole role) {
        ApplicationRoleAssignment assignment = new ApplicationRoleAssignment();
        assignment.setActorId(testIdentity.getMainActorId());
        assignment.setRoleId(role.getStringId());
        roleAssignmentRepository.save(assignment);
    }
}
