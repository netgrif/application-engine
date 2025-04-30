package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.domain.CaseRole;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.authorization.domain.repositories.RoleRepository;
import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository;
import com.netgrif.application.engine.history.domain.actorevents.ActorAssignRoleEventLog;
import com.netgrif.application.engine.history.domain.actorevents.ActorRemoveRoleEventLog;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.UserField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class RoleServiceTest {

    @Autowired
    private TestHelper helper;

    @Autowired
    private RoleRepository repository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ImportHelper importHelper;

    @BeforeEach
    public void beforeEach() {
        helper.truncateDbs();
        repository.deleteAll();
        roleService.clearCache();
    }

    @Test
    public void testFindAll() {
        Role processRole = new ProcessRole("import_id1");
        Role caseRole = new CaseRole("import_id2", "case_id");
        repository.saveAll(List.of(processRole, caseRole));

        assert roleService.findAll().size() == 2;
    }

    @Test
    public void testFindAllById() {
        Role processRole1 = new ProcessRole("import_id1");
        Role processRole2 = new ProcessRole("import_id2");
        Role caseRole = new CaseRole("import_id3", "case_id");
        repository.saveAll(List.of(processRole1, processRole2, caseRole));

        List<Role> foundRoles = roleService.findAllById(Set.of(processRole1.getStringId(), caseRole.getStringId()));
        assert foundRoles.size() == 2;

        Set<String> foundRoleIds = foundRoles.stream().map(Role::getStringId).collect(Collectors.toSet());
        assert foundRoleIds.contains(processRole1.getStringId());
        assert !foundRoleIds.contains(processRole2.getStringId());
        assert foundRoleIds.contains(caseRole.getStringId());

        assert roleService.findAllById(Set.of()).isEmpty();
    }

    @Test
    public void testFindDefaultRole() {
        Role defaultRole = roleService.findDefaultRole();
        assert defaultRole != null;
        assert defaultRole.getImportId().equals(ProcessRole.DEFAULT_ROLE);
    }

    @Test
    public void testFindAnonymousRole() {
        Role anonymousRole = roleService.findAnonymousRole();
        assert anonymousRole != null;
        assert anonymousRole.getImportId().equals(ProcessRole.ANONYMOUS_ROLE);
    }

    @Test
    public void testFindAllApplicationRoles() {
        Role processRole1 = new ProcessRole("import_id1");
        Role appRole1 = new ApplicationRole("import_id2", "application1");
        Role appRole2 = new ApplicationRole("import_id3", "application1");
        Role caseRole = new CaseRole("import_id4", "case_id");
        repository.saveAll(List.of(processRole1, appRole1, appRole2, caseRole));

        List<ApplicationRole> appRoles = roleService.findAllApplicationRoles();
        assert appRoles.size() == 2;
        assert appRoles.stream().allMatch(role -> role.getStringId().equals(appRole1.getStringId())
                || role.getStringId().equals(appRole2.getStringId()));
    }

    @Test
    public void testExistsApplicationRoleByImportId() {
        Role appRole = new ApplicationRole("import_id1", "application1");
        Role caseRole = new CaseRole("import_id2", "case_id");
        Role processRole = new ProcessRole("import_id3");
        repository.saveAll(List.of(appRole, caseRole, processRole));

        assert roleService.existsApplicationRoleByImportId(appRole.getImportId());
        assert !roleService.existsApplicationRoleByImportId(caseRole.getImportId());
        assert !roleService.existsApplicationRoleByImportId(processRole.getImportId());
    }

    @Test
    public void testFindApplicationRoleByImportId() {
        Role appRole = new ApplicationRole("import_id1", "application1");
        Role caseRole = new CaseRole("import_id2", "case_id");
        Role processRole = new ProcessRole("import_id3");
        repository.saveAll(List.of(appRole, caseRole, processRole));

        assert roleService.findApplicationRoleByImportId(appRole.getImportId()) != null;
        assert roleService.findApplicationRoleByImportId(caseRole.getImportId()) == null;
        assert roleService.findApplicationRoleByImportId(processRole.getImportId()) == null;
    }

    @Test
    public void testFindAllProcessRoles() {
        Role processRole1 = new ProcessRole("import_id1");
        Role processRole2 = new ProcessRole("import_id2");
        Role caseRole = new CaseRole("import_id3", "case_id");
        repository.saveAll(List.of(processRole1, processRole2, caseRole));

        List<ProcessRole> processRoles = roleService.findAllProcessRoles();
        assert processRoles.size() == 2;
        assert processRoles.stream().noneMatch(role -> role.getStringId().equals(caseRole.getStringId()));
    }

    @Test
    public void testFindAllProcessRolesByImportIds() {
        Role processRole1 = new ProcessRole("import_id1");
        Role processRole2 = new ProcessRole("import_id2");
        Role caseRole = new CaseRole("import_id1", "case_id");
        repository.saveAll(List.of(processRole1, processRole2, caseRole));

        List<ProcessRole> processRoles = roleService.findAllProcessRolesByImportIds(Set.of("import_id1", "import_id2"));
        assert processRoles.size() == 2;
        assert processRoles.stream().noneMatch(role -> role.getStringId().equals(caseRole.getStringId()));
    }

    @Test
    public void testFindProcessRolesByDefaultTitle() {
        ProcessRole processRole1 = new ProcessRole("import_id1");
        processRole1.setTitle(new I18nString("testTitle"));
        ProcessRole processRole2 = new ProcessRole("import_id2");
        processRole2.setTitle(new I18nString("testTitle"));
        ProcessRole processRole3 = new ProcessRole("import_id3");
        processRole3.setTitle(new I18nString("otherTitle"));
        repository.saveAll(List.of(processRole1, processRole2, processRole3));

        List<ProcessRole> processRoles = roleService.findProcessRolesByDefaultTitle("testTitle");
        assert processRoles.size() == 2;
        assert processRoles.stream().noneMatch(role -> role.getStringId().equals(processRole3.getStringId()));
    }

    @Test
    public void testExistsProcessRoleByImportId() {
        Role processRole = new ProcessRole("import_id1");
        Role caseRole = new CaseRole("import_id2", "case_id");
        Role appRole = new ApplicationRole("import_id3", "application_id");
        repository.saveAll(List.of(processRole, caseRole, appRole));

        assert roleService.existsProcessRoleByImportId(processRole.getImportId());
        assert !roleService.existsProcessRoleByImportId(caseRole.getImportId());
        assert !roleService.existsProcessRoleByImportId(appRole.getImportId());
    }

    @Test
    public void testFindProcessRoleByImportId() {
        Role processRole = new ProcessRole("import_id1");
        Role caseRole = new CaseRole("import_id2", "case_id");
        Role appRole = new ApplicationRole("import_id3", "application_id");
        repository.saveAll(List.of(processRole, caseRole, appRole));

        assert roleService.findProcessRoleByImportId(processRole.getImportId()) != null;
        assert roleService.findProcessRoleByImportId(caseRole.getImportId()) == null;
        assert roleService.findProcessRoleByImportId(appRole.getImportId()) == null;
    }

    @Test
    public void testFindAllCaseRoles() {
        Role caseRole1 = new CaseRole("import_id1", "case_id");
        Role caseRole2 = new CaseRole("import_id2", "case_id");
        Role processRole = new ProcessRole("import_id3");
        repository.saveAll(List.of(processRole, caseRole1, caseRole2));

        List<CaseRole> caseRoles = roleService.findAllCaseRoles();
        assert caseRoles.size() == 2;
        assert caseRoles.stream().noneMatch(role -> role.getStringId().equals(processRole.getStringId()));
    }

    @Test
    public void testFindCaseRoleByCaseIdAndImportId() {
        CaseRole caseRole1 = new CaseRole("import_id1", "case_id");
        Role caseRole2 = new CaseRole("import_id2", "case_id");
        Role processRole = new ProcessRole("import_id1");
        repository.saveAll(List.of(processRole, caseRole1, caseRole2));

        CaseRole foundCaseRole = roleService.findCaseRoleByCaseIdAndImportId("case_id", "import_id1");
        assert foundCaseRole.getImportId().equals(caseRole1.getImportId());
        assert foundCaseRole.getCaseId().equals(caseRole1.getCaseId());
    }

    @Test
    public void testSave() {
        assert roleService.save(new ProcessRole("import_id")).getId() != null;
        assert roleService.save(new CaseRole("import_id", "case_id")).getId() != null;
        assert roleService.save(new ApplicationRole("import_id", "application_id")).getId() != null;
    }

    @Test
    public void testSaveAll() {
        List<Role> roles = List.of(new ProcessRole("import_id"), new CaseRole("import_id", "case_id"),
                new ApplicationRole("import_id", "application_id"));
        assert roleService.saveAll(roles).size() == 3;
    }

    @Test
    public void testRemove() {
        Role processRole1 = new ProcessRole("import_id1");
        Role processRole2 = new ProcessRole("import_id2");
        Role caseRole = new CaseRole("import_id3", "case_id");
        repository.saveAll(List.of(processRole1, processRole2, caseRole));

        assert repository.findAll().size() == 3;
        roleService.remove(processRole1);
        List<Role> roles = repository.findAll();
        assert roles.stream().map(Role::getStringId).noneMatch(roleId -> roleId.equals(processRole1.getStringId()));
    }

    @Test
    public void testRemoveAll() {
        Role processRole1 = new ProcessRole("import_id1");
        Role processRole2 = new ProcessRole("import_id2");
        Role caseRole = new CaseRole("import_id3", "case_id");
        repository.saveAll(List.of(processRole1, processRole2, caseRole));

        assert repository.findAll().size() == 3;
        roleService.removeAll(List.of(processRole1, caseRole));
        List<Role> roles = repository.findAll();
        assert roles.size() == 1;
        assert roles.get(0).getStringId().equals(processRole2.getStringId());
    }

    @Test
    public void testRemoveAllByCase() {
        String caseId = new ObjectId().toString();
        Role caseRole1 = new CaseRole("import_id2", caseId);
        Role caseRole2 = new CaseRole("import_id3", caseId);
        Role caseRole3 = new CaseRole("import_id4", "fancyCaseId");
        repository.saveAll(List.of(caseRole1, caseRole2, caseRole3));

        assert roleService.findAll().size() == 3;
        roleService.removeAllByCase(caseId);
        assert roleService.findAll().size() == 1;
    }

    @Test
    public void testResolveCaseRolesOnCase() {
        UserListField userListField = new UserListField();
        userListField.setImportId("actor_list_id");
        UserField userField = new UserField();
        userField.setImportId("actor_id");

        Case useCase = new Case();
        useCase.getDataSet().put(userListField.getImportId(), userListField);
        useCase.getDataSet().put(userField.getImportId(), userField);

        AccessPermissions<CasePermission> netPermissions = new AccessPermissions<>();
        netPermissions.addPermission(userListField.getImportId(), CasePermission.VIEW, true);
        netPermissions.addPermission(userField.getImportId(), CasePermission.VIEW, true);

        roleService.resolveCaseRolesOnCase(useCase, netPermissions, false);

        List<CaseRole> roles = roleService.findAllCaseRoles();
        assert roles.size() == 2;
        assert roles.stream().allMatch(role -> role.getCaseId().equals(useCase.getStringId()));
        Optional<CaseRole> userFieldCaseRoleOpt = roles.stream()
                .filter(role -> role.getImportId().equals(userField.getImportId()))
                .findFirst();
        assert userFieldCaseRoleOpt.isPresent();
        Optional<CaseRole> userListFieldCaseRoleOpt = roles.stream()
                .filter(role -> role.getImportId().equals(userListField.getImportId()))
                .findFirst();
        assert userListFieldCaseRoleOpt.isPresent();

        assert userField.getCaseRoleIds().size() == 1;
        assert userField.getCaseRoleIds().contains(userFieldCaseRoleOpt.get().getStringId());
        assert userListField.getCaseRoleIds().size() == 1;
        assert userListField.getCaseRoleIds().contains(userListFieldCaseRoleOpt.get().getStringId());
        assert useCase.getCaseRolePermissions().containsKey(userFieldCaseRoleOpt.get().getStringId());
        assert useCase.getCaseRolePermissions().containsKey(userListFieldCaseRoleOpt.get().getStringId());

        Case emptyUseCase = new Case();
        emptyUseCase.setPetriNetObjectId(new ObjectId());
        AccessPermissions<CasePermission> invalidNetPermissions = new AccessPermissions<>();
        invalidNetPermissions.addPermission("nonExistingFieldId", CasePermission.VIEW, true);
        assertThrows(IllegalStateException.class, () ->
                roleService.resolveCaseRolesOnCase(emptyUseCase, invalidNetPermissions, false));
    }

    @Test
    public void testResolveCaseRolesOnTask() {
        UserListField userListField = new UserListField();
        userListField.setImportId("actor_list_id");
        UserField userField = new UserField();
        userField.setImportId("actor_id");

        Task task = Task.with().transitionId("transition_id").build();

        Case useCase = new Case();
        useCase.getDataSet().put(userListField.getImportId(), userListField);
        useCase.getDataSet().put(userField.getImportId(), userField);
        useCase.addTask(task);

        AccessPermissions<TaskPermission> transitionPermissions = new AccessPermissions<>();
        transitionPermissions.addPermission(userListField.getImportId(), TaskPermission.VIEW, true);
        transitionPermissions.addPermission(userField.getImportId(), TaskPermission.VIEW, true);

        roleService.resolveCaseRolesOnTask(useCase, task, transitionPermissions, false, false);

        List<CaseRole> roles = roleService.findAllCaseRoles();
        assert roles.size() == 2;
        assert roles.stream().allMatch(role -> role.getCaseId().equals(useCase.getStringId()));
        Optional<CaseRole> userFieldCaseRoleOpt = roles.stream()
                .filter(role -> role.getImportId().equals(userField.getImportId()))
                .findFirst();
        assert userFieldCaseRoleOpt.isPresent();
        Optional<CaseRole> userListFieldCaseRoleOpt = roles.stream()
                .filter(role -> role.getImportId().equals(userListField.getImportId()))
                .findFirst();
        assert userListFieldCaseRoleOpt.isPresent();

        assert userField.getCaseRoleIds().size() == 1;
        assert userField.getCaseRoleIds().contains(userFieldCaseRoleOpt.get().getStringId());
        assert userListField.getCaseRoleIds().size() == 1;
        assert userListField.getCaseRoleIds().contains(userListFieldCaseRoleOpt.get().getStringId());
        assert useCase.getCaseRolePermissions().isEmpty();
        assert task.getCaseRolePermissions().containsKey(userFieldCaseRoleOpt.get().getStringId());
        assert task.getCaseRolePermissions().containsKey(userListFieldCaseRoleOpt.get().getStringId());
    }

    @Test
    public void testAssignRolesToActor() {
        Identity identity = importHelper.createIdentity(IdentityParams.with()
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .password(new TextField("password"))
                .username(new TextField("email@email.com"))
                .build(), new ArrayList<>());

        Role processRole = new ProcessRole("import_id1");
        Role caseRole = new CaseRole("import_id2", "case_id");
        repository.saveAll(List.of(processRole, caseRole));

        eventLogRepository.deleteAll();
        List<Role> assignedRoles = roleService.assignRolesToActor(identity.toSession().getActiveActorId(),
                Set.of(processRole.getStringId(), caseRole.getStringId()));

        assert assignedRoles.size() == 2;
        assert assignedRoles.stream().anyMatch(role -> role.getStringId().equals(processRole.getStringId()));
        assert assignedRoles.stream().anyMatch(role -> role.getStringId().equals(caseRole.getStringId()));
        List<EventLog> eventLogs = eventLogRepository.findAll();
        assert eventLogs.size() == 1;
        ActorAssignRoleEventLog eventLogAfterFirstAssign = (ActorAssignRoleEventLog) eventLogs.get(0);
        assert eventLogAfterFirstAssign.getRoles().size() == 2;

        Role processRole2 = new ProcessRole("import_id3");
        repository.save(processRole2);
        assignedRoles = roleService.assignRolesToActor(identity.toSession().getActiveActorId(),
                Set.of(processRole.getStringId(), processRole2.getStringId()));

        assert assignedRoles.size() == 1;
        assert assignedRoles.get(0).getStringId().equals(processRole2.getStringId());
        eventLogs = eventLogRepository.findAll();
        assert eventLogs.size() == 2;
        Optional<EventLog> eventLogAfterSecondAssignOpt = eventLogs.stream()
                .filter(log -> !log.getStringId().equals(eventLogAfterFirstAssign.getStringId()))
                .findFirst();
        assert eventLogAfterSecondAssignOpt.isPresent();
        assert ((ActorAssignRoleEventLog) eventLogAfterSecondAssignOpt.get()).getRoles().size() == 1;

        Role processRole3 = new ProcessRole("import_id4");
        repository.save(processRole3);
        assertThrows(IllegalArgumentException.class, () -> roleService.assignRolesToActor(identity.toSession().getActiveActorId(),
                Set.of("nonExistingId", processRole3.getStringId())));

        assertThrows(IllegalArgumentException.class, () -> roleService.assignRolesToActor("nonExistingId",
                Set.of(processRole3.getStringId())));
    }

    @Test
    public void testRemoveRolesFromActor() {
        Identity identity = importHelper.createIdentity(IdentityParams.with()
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .password(new TextField("password"))
                .username(new TextField("email@email.com"))
                .build(), new ArrayList<>());
        Role processRole = new ProcessRole("import_id1");
        Role processRole2 = new ProcessRole("import_id2");
        Role caseRole = new CaseRole("import_id3", "case_id");
        repository.saveAll(List.of(processRole, processRole2, caseRole));
        roleService.assignRolesToActor(identity.toSession().getActiveActorId(), Set.of(processRole.getStringId(),
                caseRole.getStringId()));

        eventLogRepository.deleteAll();
        List<Role> removedRoles = roleService.removeRolesFromActor(identity.toSession().getActiveActorId(),
                Set.of(processRole.getStringId(), processRole2.getStringId()));
        assert removedRoles.size() == 1;
        assert removedRoles.get(0).getStringId().equals(processRole.getStringId());
        List<EventLog> eventLogs = eventLogRepository.findAll();
        assert eventLogs.size() == 1;
        ActorRemoveRoleEventLog eventLogAfterFirstRemoval = (ActorRemoveRoleEventLog) eventLogs.get(0);
        assert eventLogAfterFirstRemoval.getRoles().size() == 1;

        assertThrows(IllegalArgumentException.class, () -> roleService.removeRolesFromActor(identity.toSession().getActiveActorId(),
                Set.of("nonExistingId")));

        assertThrows(IllegalArgumentException.class, () -> roleService.assignRolesToActor("nonExistingId",
                Set.of(caseRole.getStringId())));
    }
}
