package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

        login();
    }

    @Test
    public void canCallCreate() {
        // order of assertions is important!

        assert !authorizationService.canCallCreate(null);
        assert !authorizationService.canCallCreate(testProcess.getStringId());

        Role allowCreationRole = processRoleRepository.findByImportId("create_pos_role");
        RoleAssignment assignment = new ProcessRoleAssignment();
        assignment.setActorId(testIdentity.getMainActorId());
        assignment.setRoleId(allowCreationRole.getStringId());
        roleAssignmentRepository.save(assignment);

        assert authorizationService.canCallCreate(testProcess.getStringId());

        Role denyCreationRole = processRoleRepository.findByImportId("create_neg_role");
        RoleAssignment assignment2 = new ProcessRoleAssignment();
        assignment2.setActorId(testIdentity.getMainActorId());
        assignment2.setRoleId(denyCreationRole.getStringId());
        roleAssignmentRepository.save(assignment2);

        assert !authorizationService.canCallCreate(testProcess.getStringId());

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        ApplicationRoleAssignment assignment5 = new ApplicationRoleAssignment();
        assignment5.setActorId(testIdentity.getMainActorId());
        assignment5.setRoleId(adminAppRole.getStringId());
        roleAssignmentRepository.save(assignment5);

        assert authorizationService.canCallCreate(testProcess.getStringId());

        logout();
        assert !authorizationService.canCallCreate(testProcess.getStringId());
    }

    @Test
    public void canCallDelete() {
        // order of assertions is important!

        Case testCase = importHelper.createCase("test", testProcess);

        assert !authorizationService.canCallDelete(testCase.getStringId());

        Role allowRemovalRole = processRoleRepository.findByImportId("delete_pos_role");
        RoleAssignment assignment = new ProcessRoleAssignment();
        assignment.setActorId(testIdentity.getMainActorId());
        assignment.setRoleId(allowRemovalRole.getStringId());
        roleAssignmentRepository.save(assignment);

        assert authorizationService.canCallDelete(testCase.getStringId());

        Role denyRemovalRole = processRoleRepository.findByImportId("delete_neg_role");
        RoleAssignment assignment2 = new ProcessRoleAssignment();
        assignment2.setActorId(testIdentity.getMainActorId());
        assignment2.setRoleId(denyRemovalRole.getStringId());
        roleAssignmentRepository.save(assignment2);

        assert !authorizationService.canCallDelete(testCase.getStringId());

        Role allowRemovalCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "delete_pos_case_role");
        CaseRoleAssignment assignment3 = new CaseRoleAssignment();
        assignment3.setActorId(testIdentity.getMainActorId());
        assignment3.setRoleId(allowRemovalCaseRole.getStringId());
        assignment3.setCaseId(testCase.getStringId());
        roleAssignmentRepository.save(assignment3);

        assert authorizationService.canCallDelete(testCase.getStringId());

        Role denyRemovalCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "delete_neg_case_role");
        CaseRoleAssignment assignment4 = new CaseRoleAssignment();
        assignment4.setActorId(testIdentity.getMainActorId());
        assignment4.setRoleId(denyRemovalCaseRole.getStringId());
        assignment4.setCaseId(testCase.getStringId());
        roleAssignmentRepository.save(assignment4);

        assert !authorizationService.canCallDelete(testCase.getStringId());

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        ApplicationRoleAssignment assignment5 = new ApplicationRoleAssignment();
        assignment5.setActorId(testIdentity.getMainActorId());
        assignment5.setRoleId(adminAppRole.getStringId());
        roleAssignmentRepository.save(assignment5);

        assert authorizationService.canCallDelete(testCase.getStringId());


        logout();
        assert !authorizationService.canCallDelete(testCase.getStringId());
    }

    private void login() {
        LoggedIdentity loggedTest = testIdentity.toSession();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loggedTest,
                loggedTest.getPassword(), loggedTest.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private void logout() {
          SecurityContextHolder.getContext().setAuthentication(null);
    }
}
