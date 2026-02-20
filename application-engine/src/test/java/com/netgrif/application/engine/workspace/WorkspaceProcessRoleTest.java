package com.netgrif.application.engine.workspace;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkspaceProcessRoleTest {
    
    @Autowired
    private ProcessRoleService processRoleService;
    
    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;
    
    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
    }

    private void loginCustomUser(String activeWorkspaceId, boolean isAdmin) {
        LoggedUser loggedUser = new LoggedUserImpl();
        loggedUser.setId(new ObjectId());
        loggedUser.setUsername("username1");
        loggedUser.setRealmId("default");
        loggedUser.setActiveWorkspaceId(activeWorkspaceId);
        if (isAdmin) {
            Set<Authority> authorities = new HashSet<>();
            authorities.add(new AuthorityImpl(Authority.admin));
            loggedUser.setAuthoritySet(authorities);
        }
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(loggedUser, "password", null));;
    }

    private void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
    
    @Test
    public void testSave() {
        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId("default");

        logout();
        assertNotNull(processRoleService.save(processRole));

        String workspaceId1 = "workspace1";
        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId("differentWorkspace");
        loginCustomUser(workspaceId1, false);
        assertThrows(IllegalArgumentException.class, () -> processRoleService.save(processRole2));

        processRole2.setWorkspaceId(workspaceId1);
        assertNotNull(processRoleService.save(processRole2));
    }
    
    @Test
    public void testSaveAll() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);

        logout();
        List<ProcessRole> savedRoles = processRoleService.saveAll(List.of(processRole, processRole2, processRole3));
        assertEquals(3, savedRoles.size());
        processRoleRepository.deleteAll();

        loginCustomUser("wrongWorkspace", false);
        savedRoles = processRoleService.saveAll(List.of(processRole, processRole2, processRole3));
        assertEquals(0, savedRoles.size());
        processRoleRepository.deleteAll();

        loginCustomUser(workspaceId1, false);
        savedRoles = processRoleService.saveAll(List.of(processRole, processRole2, processRole3));
        assertEquals(2, savedRoles.size());
        processRoleRepository.deleteAll();

        loginCustomUser(workspaceId2, false);
        savedRoles = processRoleService.saveAll(List.of(processRole, processRole2, processRole3));
        assertEquals(1, savedRoles.size());
        processRoleRepository.deleteAll();

        loginCustomUser(workspaceId2, true);
        savedRoles = processRoleService.saveAll(List.of(processRole, processRole2, processRole3));
        assertEquals(3, savedRoles.size());
        processRoleRepository.deleteAll();
    }
    
    @Test
    public void testGetAll() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole2);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);
        processRoleRepository.save(processRole3);

        logout();
        Page<ProcessRole> resultAsPage = processRoleService.getAll(PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = processRoleService.getAll(PageRequest.of(0, 3));
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = processRoleService.getAll(PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = processRoleService.getAll(PageRequest.of(0, 3));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = processRoleService.getAll(PageRequest.of(0, 3));
        assertEquals(1, resultAsPage.getContent().size());
    }
    
    @Test
    public void testGet() {
        String workspaceId1 = "workspace1";
        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole);

        logout();
        Optional<ProcessRole> roleOpt = processRoleService.get(processRole.get_id());
        assertTrue(roleOpt.isPresent());

        loginCustomUser("wrongWorkspace", false);
        roleOpt = processRoleService.get(processRole.get_id());
        assertFalse(roleOpt.isPresent());

        loginCustomUser("wrongWorkspace", true);
        roleOpt = processRoleService.get(processRole.get_id());
        assertTrue(roleOpt.isPresent());

        loginCustomUser(workspaceId1, false);
        roleOpt = processRoleService.get(processRole.get_id());
        assertTrue(roleOpt.isPresent());
    }

    @Test
    public void testDelete() {
        String workspaceId1 = "workspace1";
        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole);

        logout();
        processRoleService.delete(processRole.getStringId());
        assertFalse(processRoleRepository.findByCompositeId(processRole.getStringId()).isPresent());

        processRoleRepository.save(processRole);
        loginCustomUser("wrongWorkspace", false);
        processRoleService.delete(processRole.getStringId());
        assertTrue(processRoleRepository.findByCompositeId(processRole.getStringId()).isPresent());

        loginCustomUser("wrongWorkspace", true);
        processRoleService.delete(processRole.getStringId());
        assertFalse(processRoleRepository.findByCompositeId(processRole.getStringId()).isPresent());

        processRoleService.delete(processRole.getStringId());
        loginCustomUser(workspaceId1, false);
        processRoleService.delete(processRole.getStringId());
        assertFalse(processRoleRepository.findByCompositeId(processRole.getStringId()).isPresent());
    }

    @Test
    public void testDeleteAll() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);

        processRoleRepository.saveAll(List.of(processRole, processRole2, processRole3));

        logout();
        assertEquals(3, processRoleRepository.count());
        processRoleService.deleteAll();
        assertEquals(0, processRoleRepository.count());

        processRoleRepository.saveAll(List.of(processRole, processRole2, processRole3));

        loginCustomUser("wrongWorkspace", false);
        assertEquals(3, processRoleRepository.count());
        processRoleService.deleteAll();
        assertEquals(3, processRoleRepository.count());

        loginCustomUser("wrongWorkspace", true);
        processRoleService.deleteAll();
        assertEquals(0, processRoleRepository.count());

        processRoleRepository.saveAll(List.of(processRole, processRole2, processRole3));

        loginCustomUser(workspaceId1, false);
        assertEquals(3, processRoleRepository.count());
        processRoleService.deleteAll();
        assertEquals(1, processRoleRepository.count());

        loginCustomUser(workspaceId2, false);
        processRoleService.deleteAll();
        assertEquals(0, processRoleRepository.count());
    }

    @Test
    public void testDeleteAllWithIds() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);

        processRoleRepository.saveAll(List.of(processRole, processRole2, processRole3));
        List<String> ids = List.of(processRole.getStringId(), processRole2.getStringId(), processRole3.getStringId());

        logout();
        assertEquals(3, processRoleRepository.count());
        processRoleService.deleteAll(ids);
        assertEquals(0, processRoleRepository.count());

        processRoleRepository.saveAll(List.of(processRole, processRole2, processRole3));

        loginCustomUser("wrongWorkspace", false);
        assertEquals(3, processRoleRepository.count());
        processRoleService.deleteAll(ids);
        assertEquals(3, processRoleRepository.count());

        loginCustomUser("wrongWorkspace", true);
        processRoleService.deleteAll(ids);
        assertEquals(0, processRoleRepository.count());

        processRoleRepository.saveAll(List.of(processRole, processRole2, processRole3));

        loginCustomUser(workspaceId1, false);
        assertEquals(3, processRoleRepository.count());
        processRoleService.deleteAll(ids);
        assertEquals(1, processRoleRepository.count());

        loginCustomUser(workspaceId2, false);
        processRoleService.deleteAll(ids);
        assertEquals(0, processRoleRepository.count());
    }

    @Test
    public void testAssignRolesToUser() {
        AbstractUser user = new User();
        user.setRealmId("default");
        String workspaceId1 = "workspace1";
        ProcessRole role = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        role.setWorkspaceId(workspaceId1);
        processRoleRepository.save(role);

        logout();
        assertTrue(user.getProcessRoles().isEmpty());
        processRoleService.assignRolesToUser(user, List.of(role.get_id()));
        assertEquals(1, userService.findById(user.getStringId(), user.getRealmId()).getProcessRoles().size());

        user.setProcessRoles(new HashSet<>());
        user = userService.saveUser(user);
        assertTrue(user.getProcessRoles().isEmpty());

        loginCustomUser("wrongWorkspace", false);
        AbstractUser finalUser = user;
        assertThrows(IllegalArgumentException.class, () -> processRoleService.assignRolesToUser(finalUser, List.of(role.get_id())));

        loginCustomUser("wrongWorkspace", true);
        processRoleService.assignRolesToUser(user, List.of(role.get_id()));
        assertEquals(1, userService.findById(user.getStringId(), user.getRealmId()).getProcessRoles().size());

        user.setProcessRoles(new HashSet<>());
        user = userService.saveUser(user);
        assertTrue(user.getProcessRoles().isEmpty());

        loginCustomUser(workspaceId1, false);
        processRoleService.assignRolesToUser(user, List.of(role.get_id()));
        assertEquals(1, userService.findById(user.getStringId(), user.getRealmId()).getProcessRoles().size());
    }

    @Test
    public void testAssignRolesToGroup() {
        Group group = new Group("group1", "default");
        String workspaceId1 = "workspace1";
        ProcessRole role = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        role.setWorkspaceId(workspaceId1);
        processRoleRepository.save(role);

        logout();
        assertTrue(group.getProcessRoles().isEmpty());
        processRoleService.assignRolesToGroup(group, List.of(role.get_id()));
        assertEquals(1, groupService.findById(group.getStringId()).getProcessRoles().size());

        group.setProcessRoles(new HashSet<>());
        group = groupService.save(group);
        assertTrue(group.getProcessRoles().isEmpty());

        loginCustomUser("wrongWorkspace", false);
        Group finalGroup = group;
        assertThrows(IllegalArgumentException.class, () -> processRoleService.assignRolesToGroup(finalGroup, List.of(role.get_id())));

        loginCustomUser("wrongWorkspace", true);
        processRoleService.assignRolesToGroup(group, List.of(role.get_id()));
        assertEquals(1, groupService.findById(group.getStringId()).getProcessRoles().size());

        group.setProcessRoles(new HashSet<>());
        group = groupService.save(group);
        assertTrue(group.getProcessRoles().isEmpty());

        loginCustomUser(workspaceId1, false);
        processRoleService.assignRolesToGroup(group, List.of(role.get_id()));
        assertEquals(1, groupService.findById(group.getStringId()).getProcessRoles().size());
    }

    @Test
    public void testGetDefaultRole() {
        // todo 2072
    }

    @Test
    public void testGetAnonymousRole() {
        // todo 2072
    }

    @Test
    public void testFindAll() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole2);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);
        processRoleRepository.save(processRole3);

        logout();
        Page<ProcessRole> resultAsPage = processRoleService.findAll(PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = processRoleService.findAll(PageRequest.of(0, 3));
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = processRoleService.findAll(PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = processRoleService.findAll(PageRequest.of(0, 3));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = processRoleService.findAll(PageRequest.of(0, 3));
        assertEquals(1, resultAsPage.getContent().size());
    }

    @Test
    public void testFindAllByIds() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole2);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);
        processRoleRepository.save(processRole3);

        List<ProcessResourceId> ids = List.of(processRole.get_id(), processRole2.get_id(), processRole3.get_id());

        logout();
        List<ProcessRole> resultAsPage = processRoleService.findAllByIds(ids);
        assertEquals(3, resultAsPage.size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = processRoleService.findAllByIds(ids);
        assertEquals(0, resultAsPage.size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = processRoleService.findAllByIds(ids);
        assertEquals(3, resultAsPage.size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = processRoleService.findAllByIds(ids);
        assertEquals(2, resultAsPage.size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = processRoleService.findAllByIds(ids);
        assertEquals(1, resultAsPage.size());
    }

    @Test
    public void testFindById() {
        String workspaceId1 = "workspace1";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole);

        logout();
        ProcessRole result = processRoleService.findById(processRole.get_id());
        assertNotNull(result);

        loginCustomUser("wrongWorkspace", false);
        result = processRoleService.findById(processRole.get_id());
        assertNull(result);

        loginCustomUser("wrongWorkspace", true);
        result = processRoleService.findById(processRole.get_id());
        assertNotNull(result);

        loginCustomUser(workspaceId1, false);
        result = processRoleService.findById(processRole.get_id());
        assertNotNull(result);
    }

    @Test
    public void testFindAllByDefaultName() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";
        String defaultName = "name1";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRole.setName(defaultName);
        processRoleRepository.save(processRole);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);
        processRole2.setName(defaultName);
        processRoleRepository.save(processRole2);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);
        processRole3.setName(defaultName);
        processRoleRepository.save(processRole3);

        logout();
        Page<ProcessRole> resultAsPage = processRoleService.findAllByDefaultName(defaultName, PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = processRoleService.findAllByDefaultName(defaultName, PageRequest.of(0, 3));
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = processRoleService.findAllByDefaultName(defaultName, PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = processRoleService.findAllByDefaultName(defaultName, PageRequest.of(0, 3));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = processRoleService.findAllByDefaultName(defaultName, PageRequest.of(0, 3));
        assertEquals(1, resultAsPage.getContent().size());
    }

    @Test
    public void testFindAllByImportId() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";
        String importId = "importId1";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRole.setImportId(importId);
        processRoleRepository.save(processRole);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);
        processRole2.setImportId(importId);
        processRoleRepository.save(processRole2);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);
        processRole3.setImportId(importId);
        processRoleRepository.save(processRole3);

        logout();
        Page<ProcessRole> resultAsPage = processRoleService.findAllByImportId(importId, PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = processRoleService.findAllByImportId(importId, PageRequest.of(0, 3));
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = processRoleService.findAllByImportId(importId, PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = processRoleService.findAllByImportId(importId, PageRequest.of(0, 3));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = processRoleService.findAllByImportId(importId, PageRequest.of(0, 3));
        assertEquals(1, resultAsPage.getContent().size());
    }

    @Test
    public void testFindByImportId() {
        String workspaceId1 = "workspace1";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRole.setImportId("importId1");
        processRoleRepository.save(processRole);

        logout();
        ProcessRole result = processRoleService.findByImportId(processRole.getImportId());
        assertNotNull(result);

        loginCustomUser("wrongWorkspace", false);
        result = processRoleService.findByImportId(processRole.getImportId());
        assertNull(result);

        loginCustomUser("wrongWorkspace", true);
        result = processRoleService.findByImportId(processRole.getImportId());
        assertNotNull(result);

        loginCustomUser(workspaceId1, false);
        result = processRoleService.findByImportId(processRole.getImportId());
        assertNotNull(result);
    }

    @Test
    public void testFindByIdString() {
        String workspaceId1 = "workspace1";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole);

        logout();
        ProcessRole result = processRoleService.findById(processRole.getStringId());
        assertNotNull(result);

        loginCustomUser("wrongWorkspace", false);
        result = processRoleService.findById(processRole.getStringId());
        assertNull(result);

        loginCustomUser("wrongWorkspace", true);
        result = processRoleService.findById(processRole.getStringId());
        assertNotNull(result);

        loginCustomUser(workspaceId1, false);
        result = processRoleService.findById(processRole.getStringId());
        assertNotNull(result);
    }

    @Test
    public void testFindByIds() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);
        processRoleRepository.save(processRole2);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);
        processRoleRepository.save(processRole3);

        List<String> ids = List.of(processRole.getStringId(), processRole2.getStringId(), processRole3.getStringId());

        logout();
        List<ProcessRole> resultAsPage = processRoleService.findByIds(ids);
        assertEquals(3, resultAsPage.size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = processRoleService.findByIds(ids);
        assertEquals(0, resultAsPage.size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = processRoleService.findByIds(ids);
        assertEquals(3, resultAsPage.size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = processRoleService.findByIds(ids);
        assertEquals(2, resultAsPage.size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = processRoleService.findByIds(ids);
        assertEquals(1, resultAsPage.size());
    }

    @Test
    public void testFindAllGlobalRoles() {
        processRoleRepository.deleteAll();
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        ProcessRole processRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole.setWorkspaceId(workspaceId1);
        processRole.setGlobal(true);
        processRoleRepository.save(processRole);

        ProcessRole processRole2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole2.setWorkspaceId(workspaceId1);
        processRole2.setGlobal(true);
        processRoleRepository.save(processRole2);

        ProcessRole processRole3 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        processRole3.setWorkspaceId(workspaceId2);
        processRole3.setGlobal(true);
        processRoleRepository.save(processRole3);

        logout();
        Page<ProcessRole> resultAsPage = processRoleService.findAllGlobalRoles(PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = processRoleService.findAllGlobalRoles(PageRequest.of(0, 3));
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = processRoleService.findAllGlobalRoles(PageRequest.of(0, 3));
        assertEquals(3, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = processRoleService.findAllGlobalRoles(PageRequest.of(0, 3));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = processRoleService.findAllGlobalRoles(PageRequest.of(0, 3));
        assertEquals(1, resultAsPage.getContent().size());
    }

    @Test
    public void testDeleteRolesOfNet() {
        // todo 2072
    }
}
