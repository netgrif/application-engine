package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authorization.domain.CaseRole;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.authorization.domain.repositories.RoleRepository;
import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository;
import com.netgrif.application.engine.history.domain.userevents.UserAssignRoleEventLog;
import com.netgrif.application.engine.history.domain.userevents.UserRemoveRoleEventLog;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.UserField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import com.netgrif.application.engine.startup.AnonymousRoleRunner;
import com.netgrif.application.engine.startup.DefaultRoleRunner;
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
    private DefaultRoleRunner defaultRoleRunner;

    @Autowired
    private AnonymousRoleRunner anonymousRoleRunner;

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
    public void testFindDefaultRole() throws Exception {
        assertThrows(IllegalStateException.class, () -> roleService.findDefaultRole());

        defaultRoleRunner.run();

        Role defaultRole = roleService.findDefaultRole();
        assert defaultRole != null;
        assert defaultRole.getImportId().equals(ProcessRole.DEFAULT_ROLE);
    }

    @Test
    public void testFindAnonymousRole() throws Exception {
        assertThrows(IllegalStateException.class, () -> roleService.findAnonymousRole());

        anonymousRoleRunner.run();

        Role anonymousRole = roleService.findAnonymousRole();
        assert anonymousRole != null;
        assert anonymousRole.getImportId().equals(ProcessRole.ANONYMOUS_ROLE);
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
        repository.saveAll(List.of(processRole, caseRole));

        assert roleService.existsProcessRoleByImportId("import_id1");
        assert !roleService.existsProcessRoleByImportId("import_id2");
    }

    @Test
    public void testFindProcessRoleByImportId() {
        Role processRole = new ProcessRole("import_id1");
        Role caseRole = new CaseRole("import_id2", "case_id");
        repository.saveAll(List.of(processRole, caseRole));

        assert roleService.findProcessRoleByImportId("import_id1") != null;
        assert roleService.findProcessRoleByImportId("import_id2") == null;
    }

    @Test
    public void testSave() {
        assert roleService.save(new ProcessRole("import_id")).getId() != null;
        assert roleService.save(new CaseRole("import_id", "case_id")).getId() != null;
    }

    @Test
    public void testSaveAll() {
        List<Role> roles = List.of(new ProcessRole("import_id"), new CaseRole("import_id", "case_id"));
        assert roleService.saveAll(roles).size() == 2;
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
        UserListField userlistField = new UserListField();
        userlistField.setImportId("user_list_id");
        UserField userField = new UserField();
        userField.setImportId("user_id");

        Case useCase = new Case();
        useCase.getDataSet().put(userlistField.getImportId(), userlistField);
        useCase.getDataSet().put(userField.getImportId(), userField);

        AccessPermissions<CasePermission> netPermissions = new AccessPermissions<>();
        netPermissions.addPermission(userlistField.getImportId(), CasePermission.VIEW, true);
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
                .filter(role -> role.getImportId().equals(userlistField.getImportId()))
                .findFirst();
        assert userListFieldCaseRoleOpt.isPresent();

        assert userField.getCaseRoleIds().size() == 1;
        assert userField.getCaseRoleIds().contains(userFieldCaseRoleOpt.get().getStringId());
        assert userlistField.getCaseRoleIds().size() == 1;
        assert userlistField.getCaseRoleIds().contains(userListFieldCaseRoleOpt.get().getStringId());
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
        UserListField userlistField = new UserListField();
        userlistField.setImportId("user_list_id");
        UserField userField = new UserField();
        userField.setImportId("user_id");

        Task task = Task.with().transitionId("transition_id").build();

        Case useCase = new Case();
        useCase.getDataSet().put(userlistField.getImportId(), userlistField);
        useCase.getDataSet().put(userField.getImportId(), userField);
        useCase.addTask(task);

        AccessPermissions<TaskPermission> transitionPermissions = new AccessPermissions<>();
        transitionPermissions.addPermission(userlistField.getImportId(), TaskPermission.VIEW, true);
        transitionPermissions.addPermission(userField.getImportId(), TaskPermission.VIEW, true);

        roleService.resolveCaseRolesOnTask(useCase, task, transitionPermissions, false);

        List<CaseRole> roles = roleService.findAllCaseRoles();
        assert roles.size() == 2;
        assert roles.stream().allMatch(role -> role.getCaseId().equals(useCase.getStringId()));
        Optional<CaseRole> userFieldCaseRoleOpt = roles.stream()
                .filter(role -> role.getImportId().equals(userField.getImportId()))
                .findFirst();
        assert userFieldCaseRoleOpt.isPresent();
        Optional<CaseRole> userListFieldCaseRoleOpt = roles.stream()
                .filter(role -> role.getImportId().equals(userlistField.getImportId()))
                .findFirst();
        assert userListFieldCaseRoleOpt.isPresent();

        assert userField.getCaseRoleIds().size() == 1;
        assert userField.getCaseRoleIds().contains(userFieldCaseRoleOpt.get().getStringId());
        assert userlistField.getCaseRoleIds().size() == 1;
        assert userlistField.getCaseRoleIds().contains(userListFieldCaseRoleOpt.get().getStringId());
        assert useCase.getCaseRolePermissions().isEmpty();
        assert task.getCaseRolePermissions().containsKey(userFieldCaseRoleOpt.get().getStringId());
        assert task.getCaseRolePermissions().containsKey(userListFieldCaseRoleOpt.get().getStringId());
    }

    @Test
    public void testAssignRolesToUser() {
        User user = new User("email", "password", "username", "surname");
        importHelper.createUser(user, new Authority[]{}, new ProcessRole[]{});

        Role processRole = new ProcessRole("import_id1");
        Role caseRole = new CaseRole("import_id2", "case_id");
        repository.saveAll(List.of(processRole, caseRole));

        eventLogRepository.deleteAll();
        List<Role> assignedRoles = roleService.assignRolesToActor(user.getStringId(),
                Set.of(processRole.getStringId(), caseRole.getStringId()));

        assert assignedRoles.size() == 2;
        assert assignedRoles.stream().anyMatch(role -> role.getStringId().equals(processRole.getStringId()));
        assert assignedRoles.stream().anyMatch(role -> role.getStringId().equals(caseRole.getStringId()));
        List<EventLog> eventLogs = eventLogRepository.findAll();
        assert eventLogs.size() == 1;
        UserAssignRoleEventLog eventLogAfterFirstAssign = (UserAssignRoleEventLog) eventLogs.get(0);
        assert eventLogAfterFirstAssign.getRoles().size() == 2;

        Role processRole2 = new ProcessRole("import_id3");
        repository.save(processRole2);
        assignedRoles = roleService.assignRolesToActor(user.getStringId(),
                Set.of(processRole.getStringId(), processRole2.getStringId()));

        assert assignedRoles.size() == 1;
        assert assignedRoles.get(0).getStringId().equals(processRole2.getStringId());
        eventLogs = eventLogRepository.findAll();
        assert eventLogs.size() == 2;
        Optional<EventLog> eventLogAfterSecondAssignOpt = eventLogs.stream()
                .filter(log -> !log.getStringId().equals(eventLogAfterFirstAssign.getStringId()))
                .findFirst();
        assert eventLogAfterSecondAssignOpt.isPresent();
        assert ((UserAssignRoleEventLog) eventLogAfterSecondAssignOpt.get()).getRoles().size() == 1;

        Role processRole3 = new ProcessRole("import_id4");
        repository.save(processRole3);
        assertThrows(IllegalArgumentException.class, () -> roleService.assignRolesToActor(user.getStringId(),
                Set.of("nonExistingId", processRole3.getStringId())));

        assertThrows(IllegalArgumentException.class, () -> roleService.assignRolesToActor("nonExistingId",
                Set.of(processRole3.getStringId())));
    }

    @Test
    public void testRemoveRolesFromUser() {
        User user = new User("email", "password", "username", "surname");
        importHelper.createUser(user, new Authority[]{}, new ProcessRole[]{});
        Role processRole = new ProcessRole("import_id1");
        Role processRole2 = new ProcessRole("import_id2");
        Role caseRole = new CaseRole("import_id3", "case_id");
        repository.saveAll(List.of(processRole, processRole2, caseRole));
        roleService.assignRolesToActor(user.getStringId(), Set.of(processRole.getStringId(), caseRole.getStringId()));

        eventLogRepository.deleteAll();
        List<Role> removedRoles = roleService.removeRolesFromActor(user.getStringId(),
                Set.of(processRole.getStringId(), processRole2.getStringId()));
        assert removedRoles.size() == 1;
        assert removedRoles.get(0).getStringId().equals(processRole.getStringId());
        List<EventLog> eventLogs = eventLogRepository.findAll();
        assert eventLogs.size() == 1;
        UserRemoveRoleEventLog eventLogAfterFirstRemoval = (UserRemoveRoleEventLog) eventLogs.get(0);
        assert eventLogAfterFirstRemoval.getRoles().size() == 1;

        assertThrows(IllegalArgumentException.class, () -> roleService.removeRolesFromActor(user.getStringId(),
                Set.of("nonExistingId")));

        assertThrows(IllegalArgumentException.class, () -> roleService.assignRolesToActor("nonExistingId",
                Set.of(caseRole.getStringId())));
    }
}
