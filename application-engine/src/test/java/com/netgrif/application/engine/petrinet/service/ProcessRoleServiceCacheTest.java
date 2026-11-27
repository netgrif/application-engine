package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.auth.config.AuthCacheProperties;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.RoleActionsRunner;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests of the in-memory cache of roles resolved by id in {@link ProcessRoleService}.
 */
@ExtendWith(MockitoExtension.class)
class ProcessRoleServiceCacheTest {

    @Mock
    private ProcessRoleRepository processRoleRepository;

    @Mock
    private PetriNetRepository netRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private RoleActionsRunner roleActionsRunner;

    @Mock
    private IPetriNetService petriNetService;

    @Mock
    private UserService userService;

    @Mock
    private ISecurityContextService securityContextService;

    @Mock
    private GroupService groupService;

    @Mock
    private RealmService realmService;

    @Mock
    private IWorkflowService workflowService;

    @Mock
    private ITaskService taskService;

    private ProcessRoleService processRoleService;

    private TestProcessRole role;
    private ObjectId roleObjectId;

    @BeforeEach
    void setUp() {
        processRoleService = createService(new AuthCacheProperties());
        role = new TestProcessRole();
        roleObjectId = new ObjectId(role.getStringId().split("-")[1]);
    }

    private ProcessRoleService createService(AuthCacheProperties properties) {
        return new ProcessRoleService(processRoleRepository, netRepository, publisher, roleActionsRunner, petriNetService,
                userService, securityContextService, groupService, realmService, new PaginationProperties(), workflowService,
                taskService, properties);
    }

    /**
     * Loads the role through the cache once; the following repository interactions are counted from here.
     */
    private void populateCache() {
        when(processRoleRepository.findByIdObjectId(roleObjectId)).thenReturn(Optional.of(role));
        assertSame(role, processRoleService.findById(role.getStringId()));
    }

    private void assertCacheWasInvalidated() {
        assertSame(role, processRoleService.findById(role.getStringId()));
        verify(processRoleRepository, times(2)).findByIdObjectId(roleObjectId);
    }

    @Test
    void findByIdLoadsRoleOnlyOnce() {
        populateCache();

        assertSame(role, processRoleService.findById(role.getStringId()));

        verify(processRoleRepository, times(1)).findByIdObjectId(roleObjectId);
    }

    @Test
    void exposedCacheIsTheOneUsedByFindById() {
        // subclasses (e.g. cluster services) populate and clear the same cache through the protected getter
        processRoleService.getRoleByIdCache().get(role.getStringId(), id -> role);

        assertSame(role, processRoleService.findById(role.getStringId()));

        verify(processRoleRepository, never()).findByIdObjectId(any());

        processRoleService.getRoleByIdCache().clear();
        when(processRoleRepository.findByIdObjectId(roleObjectId)).thenReturn(Optional.of(role));
        assertSame(role, processRoleService.findById(role.getStringId()));
        verify(processRoleRepository, times(1)).findByIdObjectId(roleObjectId);
    }

    @Test
    void findByIdDoesNotCacheMissingRole() {
        when(processRoleRepository.findByIdObjectId(roleObjectId)).thenReturn(Optional.empty());

        assertEquals(null, processRoleService.findById(role.getStringId()));
        assertEquals(null, processRoleService.findById(role.getStringId()));

        verify(processRoleRepository, times(2)).findByIdObjectId(roleObjectId);
    }

    @Test
    void findByIdDoesNotCacheWhenTtlIsZero() {
        AuthCacheProperties properties = new AuthCacheProperties();
        properties.getProcessRole().setTtl(Duration.ZERO);
        processRoleService = createService(properties);
        when(processRoleRepository.findByIdObjectId(roleObjectId)).thenReturn(Optional.of(role));

        processRoleService.findById(role.getStringId());
        processRoleService.findById(role.getStringId());

        verify(processRoleRepository, times(2)).findByIdObjectId(roleObjectId);
    }

    @Test
    void findByIdRejectsMalformedId() {
        assertThrows(IllegalArgumentException.class, () -> processRoleService.findById("malformed"));
    }

    @Test
    void saveInvalidatesCache() {
        populateCache();
        when(processRoleRepository.save(role)).thenReturn(role);

        assertSame(role, processRoleService.save(role));

        assertCacheWasInvalidated();
    }

    @Test
    void saveInvalidatesCacheWhenWriteFails() {
        populateCache();
        when(processRoleRepository.save(role)).thenThrow(new IllegalStateException("write failed"));

        assertThrows(IllegalStateException.class, () -> processRoleService.save(role));

        assertCacheWasInvalidated();
    }

    @Test
    void saveAllInvalidatesCache() {
        populateCache();
        when(processRoleRepository.save(role)).thenReturn(role);

        assertEquals(List.of(role), processRoleService.saveAll(List.of(role)));

        assertCacheWasInvalidated();
    }

    @Test
    void saveAllInvalidatesCacheWhenFailingAfterPartialWrite() {
        populateCache();
        TestProcessRole failing = new TestProcessRole();
        when(processRoleRepository.save(role)).thenReturn(role);
        when(processRoleRepository.save(failing)).thenThrow(new IllegalStateException("write failed"));

        assertThrows(IllegalStateException.class, () -> processRoleService.saveAll(List.of(role, failing)));

        assertCacheWasInvalidated();
    }

    @Test
    void saveAllSkipsGlobalRoleThatAlreadyExists() {
        TestProcessRole global = new TestProcessRole();
        global.setGlobal(true);
        when(processRoleRepository.findAllByImportId(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(global)));

        assertTrue(processRoleService.saveAll(List.of(global)).isEmpty());
    }

    @Test
    void deleteInvalidatesCache() {
        populateCache();
        when(processRoleRepository.findByCompositeId(role.getStringId())).thenReturn(Optional.of(role));

        processRoleService.delete(role.getStringId());

        verify(processRoleRepository).delete(role);
        assertCacheWasInvalidated();
    }

    @Test
    void deleteIgnoresNullId() {
        populateCache();

        processRoleService.delete(null);

        // nothing was deleted, so the cached role is still served
        assertSame(role, processRoleService.findById(role.getStringId()));
        verify(processRoleRepository, times(1)).findByIdObjectId(roleObjectId);
    }

    @Test
    void deleteAllByIdsInvalidatesCache() {
        populateCache();
        List<String> ids = List.of(role.getStringId());
        when(processRoleRepository.findAllByIdsSet(ids)).thenReturn(Set.of(role));

        processRoleService.deleteAll(ids);

        verify(processRoleRepository).deleteAll(Set.of(role));
        assertCacheWasInvalidated();
    }

    @Test
    void deleteAllInvalidatesCache() {
        populateCache();

        processRoleService.deleteAll();

        verify(processRoleRepository).deleteAll();
        assertCacheWasInvalidated();
    }

    @Test
    void clearCacheInvalidatesCache() {
        populateCache();

        processRoleService.clearCache();

        assertCacheWasInvalidated();
    }

    @Test
    void deleteGlobalRoleInvalidatesCache() {
        role.setGlobal(true);
        populateCache();
        when(petriNetService.findAllByRoleId(any(), any(Pageable.class))).thenReturn(new PageImpl<PetriNet>(List.of()));
        when(realmService.getSmallRealm(any(Pageable.class))).thenReturn(new PageImpl<Realm>(List.of()));

        processRoleService.deleteGlobalRole(role.getStringId(), new TestLoggedUser());

        verify(processRoleRepository).delete(role);
        assertCacheWasInvalidated();
    }

    @Test
    void deleteRolesOfNetInvalidatesCache() {
        populateCache();
        PetriNet net = mock(PetriNet.class);
        when(net.getStringId()).thenReturn("net-id");
        when(net.getVersion()).thenReturn(mock(Version.class));
        when(net.getRoles()).thenReturn(new LinkedHashMap<>(Map.of(role.getStringId(), role)));
        when(netRepository.findById("net-id")).thenReturn(Optional.of(net));
        when(realmService.getSmallRealm(any(Pageable.class))).thenReturn(new PageImpl<Realm>(List.of()));
        when(groupService.findAllByProcessRoles(any(), any(Pageable.class))).thenReturn(new PageImpl<Group>(List.of()));

        processRoleService.deleteRolesOfNet(net, new TestLoggedUser());

        verify(processRoleRepository).deleteAllBy_idIn(List.of(role.get_id()));
        assertCacheWasInvalidated();
    }

    private static class TestProcessRole extends ProcessRole {

        TestProcessRole() {
            super();
        }
    }

    private static class TestLoggedUser extends LoggedUser {

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public void setPassword(String password) {
        }
    }
}
