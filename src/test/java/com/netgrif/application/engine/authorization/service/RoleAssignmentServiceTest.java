package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authorization.domain.*;
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class RoleAssignmentServiceTest {

    @Autowired
    private RoleAssignmentService roleAssignmentService;

    @Autowired
    private RoleAssignmentRepository repository;

    @Autowired
    private TestHelper helper;

    @BeforeEach
    public void beforeEach() {
        helper.truncateDbs();
        repository.deleteAll();
    }

    @Test
    public void testFindAllByActorIdAndRoleIdIn() {
        String actorId = "actorId";
        String roleId1 = "roleId1";
        String roleId2 = "roleId2";
        String roleId3 = "roleId3";
        RoleAssignment assignment1 = new ProcessRoleAssignment();
        assignment1.setActorId(actorId);
        assignment1.setRoleId(roleId1);
        RoleAssignment assignment2 = new ProcessRoleAssignment();
        assignment2.setActorId(actorId);
        assignment2.setRoleId(roleId2);
        RoleAssignment assignment3 = new ProcessRoleAssignment();
        assignment3.setActorId(actorId);
        assignment3.setRoleId(roleId3);
        repository.saveAll(List.of(assignment1, assignment2, assignment3));

        List<RoleAssignment> assignments = roleAssignmentService.findAllByActorIdAndRoleIdIn(actorId, Set.of(roleId1, roleId2));

        assert assignments.size() == 2;
        assert assignments.stream().noneMatch(assignment -> assignment.getStringId().equals(assignment3.getStringId()));
    }

    @Test
    public void testFindAllByRoleIdIn() {
        String actorId = "actorId";
        String roleId1 = "roleId1";
        String roleId2 = "roleId2";
        String roleId3 = "roleId3";
        RoleAssignment assignment1 = new ProcessRoleAssignment();
        assignment1.setActorId(actorId);
        assignment1.setRoleId(roleId1);
        RoleAssignment assignment2 = new ProcessRoleAssignment();
        assignment2.setActorId(actorId);
        assignment2.setRoleId(roleId2);
        RoleAssignment assignment3 = new ProcessRoleAssignment();
        assignment3.setActorId(actorId);
        assignment3.setRoleId(roleId3);
        repository.saveAll(List.of(assignment1, assignment2, assignment3));

        List<RoleAssignment> assignments = roleAssignmentService.findAllByRoleIdIn(Set.of(roleId1, roleId2));

        assert assignments.size() == 2;
        assert assignments.stream().noneMatch(assignment -> assignment.getStringId().equals(assignment3.getStringId()));
    }

    @Test
    public void testFindAllByActorId() {
        String actorId = "actorId";
        String roleId1 = "roleId1";
        String roleId2 = "roleId2";
        RoleAssignment assignment1 = new ProcessRoleAssignment();
        assignment1.setActorId(actorId);
        assignment1.setRoleId(roleId1);
        RoleAssignment assignment2 = new ProcessRoleAssignment();
        assignment2.setActorId(actorId);
        assignment2.setRoleId(roleId2);
        repository.saveAll(List.of(assignment1, assignment2));

        assert roleAssignmentService.findAllByActorId(actorId).size() == 2;
    }

    @Test
    public void testFindAllRoleIdsByActorId() {
        String actorId = "actorId";
        String roleId1 = "roleId1";
        String roleId2 = "roleId2";
        RoleAssignment assignment1 = new ProcessRoleAssignment();
        assignment1.setActorId(actorId);
        assignment1.setRoleId(roleId1);
        RoleAssignment assignment2 = new ProcessRoleAssignment();
        assignment2.setActorId(actorId);
        assignment2.setRoleId(roleId2);
        RoleAssignment assignment3 = new ProcessRoleAssignment();
        assignment3.setActorId("otherActorId");
        assignment3.setRoleId("role3");
        repository.saveAll(List.of(assignment1, assignment2, assignment3));

        assert roleAssignmentService.findAllRoleIdsByActorId(actorId).size() == 2;
    }

    @Test
    public void testExistsByActorAndRole() {
        String actorId = "actorId";
        String roleId1 = "roleId1";
        RoleAssignment assignment1 = new ProcessRoleAssignment();
        assignment1.setActorId(actorId);
        assignment1.setRoleId(roleId1);
        repository.saveAll(List.of(assignment1));

        assert !roleAssignmentService.existsByActorAndRole("wrongActor", "wrongRole");
        assert !roleAssignmentService.existsByActorAndRole("wrongActor", roleId1);
        assert !roleAssignmentService.existsByActorAndRole(actorId, "wrongRole");
        assert roleAssignmentService.existsByActorAndRole(actorId, roleId1);
    }

    @Test
    public void testFindApplicationAssignmentsByActor() {
        String actorId = "actorId";
        ApplicationRoleAssignment assignment1 = new ApplicationRoleAssignment();
        assignment1.setApplicationId("appId");
        assignment1.setActorId(actorId);
        ApplicationRoleAssignment assignment2 = new ApplicationRoleAssignment();
        assignment2.setApplicationId("appId");
        assignment2.setActorId(actorId);

        repository.saveAll(List.of(assignment1, assignment2));

        assert roleAssignmentService.findApplicationAssignmentsByActor("wrongActor").isEmpty();
        assert roleAssignmentService.findApplicationAssignmentsByActor(actorId).size() == 2;
    }

    @Test
    public void testCreateAssignments() {
        List<RoleAssignment> assignments = roleAssignmentService.createAssignments("actorId",
                List.of(new ProcessRole("import_id1"), new CaseRole("import_id2", "case_id")));

        assert repository.findAll().size() == 2;
        assert assignments.size() == 2;
    }

    @Test
    public void testCreateAssignment() {
        String actorId = "actorId";

        ProcessRole processRole = new ProcessRole("import_id1");
        RoleAssignment assignment = roleAssignmentService.createAssignment(actorId, processRole);
        assert assignment != null;
        assert assignment instanceof ProcessRoleAssignment;
        assert assignment.getActorId().equals(actorId);
        assert assignment.getRoleId().equals(processRole.getStringId());
        assert assignment.getRoleImportId().equals(processRole.getImportId());
        assert repository.existsById(assignment.getStringId());

        CaseRole caseRole = new CaseRole("import_id2", "case_id");
        assignment = roleAssignmentService.createAssignment(actorId, caseRole);
        assert assignment != null;
        assert assignment instanceof CaseRoleAssignment;
        assert assignment.getActorId().equals(actorId);
        assert assignment.getRoleId().equals(caseRole.getStringId());
        assert assignment.getRoleImportId().equals(caseRole.getImportId());
        assert ((CaseRoleAssignment) assignment).getCaseId().equals(caseRole.getCaseId());
        assert repository.existsById(assignment.getStringId());
    }

    @Test
    public void testRemoveAssignments() {
        String actorId = "actor_id";
        String processRoleId = "role1";
        String processRoleId2 = "role2";
        String caseRoleId = "role3";
        RoleAssignment processRoleAssignment = new ProcessRoleAssignment();
        processRoleAssignment.setActorId(actorId);
        processRoleAssignment.setRoleId(processRoleId);
        RoleAssignment processRoleAssignment2 = new ProcessRoleAssignment();
        processRoleAssignment2.setActorId("someOtherActorId");
        processRoleAssignment2.setRoleId(processRoleId2);
        CaseRoleAssignment caseRoleAssignment = new CaseRoleAssignment();
        caseRoleAssignment.setActorId(actorId);
        caseRoleAssignment.setRoleId(caseRoleId);
        repository.saveAll(List.of(processRoleAssignment, processRoleAssignment2, caseRoleAssignment));

        List<RoleAssignment> removedAssignments = roleAssignmentService.removeAssignments(actorId,
                Set.of(processRoleId, caseRoleId, processRoleId2));

        assert removedAssignments.size() == 2;
        assert removedAssignments.stream().noneMatch(assignment ->
                assignment.getStringId().equals(processRoleAssignment2.getStringId()));

        List<RoleAssignment> persistedAssignments = repository.findAll();
        assert persistedAssignments.size() == 1;
        assert persistedAssignments.get(0).getStringId().equals(processRoleAssignment2.getStringId());
    }

    @Test
    public void testRemoveAssignment() {
        String actorId = "actor_id";
        String roleId = "role1";
        RoleAssignment processRoleAssignment = new ProcessRoleAssignment();
        processRoleAssignment.setActorId(actorId);
        processRoleAssignment.setRoleId(roleId);
        repository.save(processRoleAssignment);

        assert repository.count() == 1;
        roleAssignmentService.removeAssignment(actorId, roleId);
        assert repository.count() == 0;
    }

    @Test
    public void testRemoveAssignmentsByActor() {
        String actorId = "actor_id";
        String processRoleId = "role1";
        String processRoleId2 = "role2";
        String caseRoleId = "role3";
        RoleAssignment processRoleAssignment = new ProcessRoleAssignment();
        processRoleAssignment.setActorId(actorId);
        processRoleAssignment.setRoleId(processRoleId);
        RoleAssignment processRoleAssignment2 = new ProcessRoleAssignment();
        processRoleAssignment2.setActorId("someOtherActorId");
        processRoleAssignment2.setRoleId(processRoleId2);
        CaseRoleAssignment caseRoleAssignment = new CaseRoleAssignment();
        caseRoleAssignment.setActorId(actorId);
        caseRoleAssignment.setRoleId(caseRoleId);
        repository.saveAll(List.of(processRoleAssignment, processRoleAssignment2, caseRoleAssignment));

        assert repository.count() == 3;
        List<RoleAssignment> removedAssignments = roleAssignmentService.removeAssignmentsByActor(actorId);

        assert removedAssignments.size() == 2;
        assert removedAssignments.stream().noneMatch(assignment ->
                assignment.getStringId().equals(processRoleAssignment2.getStringId()));

        List<RoleAssignment> persistedAssignments = repository.findAll();
        assert persistedAssignments.size() == 1;
        assert persistedAssignments.get(0).getStringId().equals(processRoleAssignment2.getStringId());
    }

    @Test
    public void testRemoveAssignmentsByRole() {
        String roleId = "role1";
        String roleId2 = "role2";
        RoleAssignment assignment = new ProcessRoleAssignment();
        assignment.setRoleId(roleId);
        RoleAssignment assignment2 = new ProcessRoleAssignment();
        assignment2.setRoleId(roleId2);
        RoleAssignment assignment3 = new ProcessRoleAssignment();
        assignment3.setRoleId(roleId);
        repository.saveAll(List.of(assignment, assignment2, assignment3));

        List<RoleAssignment> removedAssignments = roleAssignmentService.removeAssignmentsByRole(roleId);

        List<RoleAssignment> persistedAssignments = repository.findAll();
        assert persistedAssignments.size() == 1;
        assert persistedAssignments.get(0).getStringId().equals(assignment2.getStringId());

        assert removedAssignments.size() == 2;
        assert removedAssignments.stream().noneMatch(removedAssignment ->
                removedAssignment.getStringId().equals(assignment2.getStringId()));
    }

    @Test
    public void testRemoveAssignmentsByRoles() {
        String roleId = "role1";
        String roleId2 = "role2";
        String roleId3 = "role3";
        RoleAssignment assignment = new ProcessRoleAssignment();
        assignment.setRoleId(roleId);
        RoleAssignment assignment2 = new ProcessRoleAssignment();
        assignment2.setRoleId(roleId2);
        RoleAssignment assignment3 = new CaseRoleAssignment();
        assignment3.setRoleId(roleId3);
        repository.saveAll(List.of(assignment, assignment2, assignment3));

        List<RoleAssignment> removedAssignments = roleAssignmentService.removeAssignmentsByRoles(Set.of(roleId, roleId2));

        List<RoleAssignment> persistedAssignments = repository.findAll();
        assert persistedAssignments.size() == 1;
        assert persistedAssignments.get(0).getStringId().equals(assignment3.getStringId());

        assert removedAssignments.size() == 2;
        assert removedAssignments.stream().noneMatch(removedAssignment ->
                removedAssignment.getStringId().equals(assignment3.getStringId()));
    }

    @Test
    public void testRemoveAssignmentsByCase() {
        String caseId = "case_id";
        CaseRoleAssignment caseRoleAssignment = new CaseRoleAssignment();
        caseRoleAssignment.setCaseId(caseId);
        RoleAssignment processRoleAssignment = new ProcessRoleAssignment();
        repository.saveAll(List.of(processRoleAssignment, caseRoleAssignment));

        List<CaseRoleAssignment> removedAssignments = roleAssignmentService.removeAssignmentsByCase(caseId);

        List<RoleAssignment> persistedAssignments = repository.findAll();
        assert persistedAssignments.size() == 1;
        assert persistedAssignments.get(0).getStringId().equals(processRoleAssignment.getStringId());

        assert removedAssignments.size() == 1;
        assert removedAssignments.stream().noneMatch(removedAssignment ->
                removedAssignment.getStringId().equals(processRoleAssignment.getStringId()));
    }
}
