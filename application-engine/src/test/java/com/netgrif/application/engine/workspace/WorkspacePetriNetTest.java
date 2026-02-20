package com.netgrif.application.engine.workspace;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import com.netgrif.application.engine.objects.workflow.domain.QCase;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workspace.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkspacePetriNetTest {

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private SuperCreatorRunner superCreatorRunner;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private IWorkflowService workflowService;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
        petriNetRepository.deleteAll();
    }

    private void loginCustomUser(String activeWorkspaceId, boolean isAdmin) {
        loginCustomUser(activeWorkspaceId, isAdmin, null);
    }

    private void loginCustomUser(String activeWorkspaceId, boolean isAdmin, Set<ProcessRole> processRoles) {
        LoggedUser loggedUser = new LoggedUserImpl();
        loggedUser.setUsername("username1");
        loggedUser.setActiveWorkspaceId(activeWorkspaceId);
        loggedUser.setProcessRoles(processRoles);
        if (isAdmin) {
            Set<Authority> authorities = new HashSet<>();
            authorities.add(new AuthorityImpl(Authority.admin));
            loggedUser.setAuthoritySet(authorities);
        }
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(loggedUser, "password", null));
    }

    private void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    public void testImportPetriNet() throws IOException, MissingPetriNetMetaDataException {
        ImportPetriNetParams params = ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build();
        PetriNet net = petriNetService.importPetriNet(params).getNet();
        assertEquals(workspaceService.getDefault().getId(), net.getWorkspaceId());
        Optional<ProcessRole> localRoleOptional = processRoleRepository.findByImportIdAndWorkspaceId("local_role", net.getWorkspaceId());
        assertTrue(localRoleOptional.isPresent());
        assertEquals(net.getWorkspaceId(), localRoleOptional.get().getWorkspaceId());
        Optional<ProcessRole> globalRoleOptional = processRoleRepository.findByImportIdAndWorkspaceId("global_global_role", net.getWorkspaceId());
        assertTrue(globalRoleOptional.isPresent());
        assertEquals(net.getWorkspaceId(), globalRoleOptional.get().getWorkspaceId());

        params = ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .releaseType(VersionType.MINOR)
                .workspaceId("anotherWorkspaceId")
                .author(superCreatorRunner.getLoggedSuper())
                .build();
        net = petriNetService.importPetriNet(params).getNet();
        assertEquals("anotherWorkspaceId", net.getWorkspaceId());
        localRoleOptional = processRoleRepository.findByImportIdAndWorkspaceId("local_role", net.getWorkspaceId());
        assertTrue(localRoleOptional.isPresent());
        assertEquals(net.getWorkspaceId(), localRoleOptional.get().getWorkspaceId());
        globalRoleOptional = processRoleRepository.findByImportIdAndWorkspaceId("global_global_role", net.getWorkspaceId());
        assertTrue(globalRoleOptional.isPresent());
        assertEquals(net.getWorkspaceId(), globalRoleOptional.get().getWorkspaceId());

        assertEquals(2, processRoleRepository.findAllByImportId("local_role", PageRequest.of(0, 3)).getContent().size());
        assertEquals(2, processRoleRepository.findAllByImportId("global_global_role", PageRequest.of(0, 3)).getContent().size());
    }

    @Test
    public void testImportSamePetriNet() throws IOException, MissingPetriNetMetaDataException {
        String netPath = "src/test/resources/petriNets/workspace_version_test.xml";
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream(netPath))
                .workspaceId("workspaceA")
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        assertNotNull(petriNetRepository.findById(net.getStringId()));

        assertThrows(IllegalArgumentException.class, () -> petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream(netPath))
                .workspaceId("workspaceA")
                .author(superCreatorRunner.getLoggedSuper())
                .build()));

        net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream(netPath))
                .workspaceId("workspaceB")
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        assertNotNull(petriNetRepository.findById(net.getStringId()));

        assertThrows(IllegalArgumentException.class, () -> petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream(netPath))
                .workspaceId("workspaceB")
                .author(superCreatorRunner.getLoggedSuper())
                .build()));
    }

    @Test
    public void testSave() {
        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId("default");
        assertNotNull(petriNetService.save(net));

        loginCustomUser("differentWorkspaceId", false);
        assertThrows(IllegalArgumentException.class, () -> petriNetService.save(net));

        loginCustomUser("default", false);
        assertNotNull(petriNetService.save(net));

        loginCustomUser("differentWorkspaceId", true);
        assertNotNull(petriNetService.save(net));

        logout();
        assertNotNull(petriNetService.save(net));

        loginCustomUser(null, false);
        assertThrows(IllegalArgumentException.class, () -> petriNetService.save(net));
    }

    @Test
    public void testGetPetriNet() {
        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId("default");
        net.setIdentifier("identifier");
        net = petriNetRepository.save(net);
        String netId = net.getStringId();

        logout();
        assertDoesNotThrow(() -> petriNetService.getPetriNet(netId));
        petriNetService.evictAllCaches();

        loginCustomUser("differentWorkspaceId", false);
        assertThrows(IllegalArgumentException.class, () -> petriNetService.getPetriNet(netId));
        petriNetService.evictAllCaches();

        loginCustomUser("differentWorkspaceId", true);
        assertDoesNotThrow(() -> petriNetService.getPetriNet(netId));
        petriNetService.evictAllCaches();

        loginCustomUser("default", false);
        assertDoesNotThrow(() -> petriNetService.getPetriNet(netId));
        petriNetService.evictAllCaches();

        Version version = net.getVersion();
        logout();
        assertNotNull(petriNetService.getPetriNet(net.getIdentifier(), version));
        petriNetService.evictAllCaches();

        loginCustomUser("differentWorkspaceId", false);
        assertNull(petriNetService.getPetriNet(net.getIdentifier(), version));
        petriNetService.evictAllCaches();

        loginCustomUser("differentWorkspaceId", true);
        assertNotNull(petriNetService.getPetriNet(net.getIdentifier(), version));
        petriNetService.evictAllCaches();

        loginCustomUser("default", false);
        assertNotNull(petriNetService.getPetriNet(net.getIdentifier(), version));
        petriNetService.evictAllCaches();
    }

    @Test
    public void testGetByIdentifier() {
        String identifier = "identifier";
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";
        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId1);
        net.setIdentifier(identifier);
        petriNetRepository.save(net);
        net.setObjectId(new ObjectId());
        petriNetRepository.save(net);

        net.setWorkspaceId(workspaceId2);
        net.setObjectId(new ObjectId());
        petriNetRepository.save(net);
        net.setObjectId(new ObjectId());
        petriNetRepository.save(net);

        assertEquals(4, petriNetRepository.count());

        logout();
        Page<PetriNet> resultPage = petriNetService.getByIdentifier(identifier, PageRequest.of(0, 4));
        assertEquals(4, resultPage.getContent().size());
        petriNetService.evictAllCaches();

        loginCustomUser(workspaceId1, false);
        resultPage = petriNetService.getByIdentifier(identifier, PageRequest.of(0, 4));
        assertEquals(2, resultPage.getContent().size());
        petriNetService.evictAllCaches();

        loginCustomUser(workspaceId1, true);
        resultPage = petriNetService.getByIdentifier(identifier, PageRequest.of(0, 4));
        assertEquals(4, resultPage.getContent().size());
        petriNetService.evictAllCaches();

        loginCustomUser(workspaceId2, false);
        resultPage = petriNetService.getByIdentifier(identifier, PageRequest.of(0, 4));
        assertEquals(2, resultPage.getContent().size());
        petriNetService.evictAllCaches();

        loginCustomUser(workspaceId2, true);
        resultPage = petriNetService.getByIdentifier(identifier, PageRequest.of(0, 4));
        assertEquals(4, resultPage.getContent().size());
        petriNetService.evictAllCaches();

        loginCustomUser("wrongWorkspaceId", false);
        resultPage = petriNetService.getByIdentifier(identifier, PageRequest.of(0, 4));
        assertTrue(resultPage.isEmpty());
        petriNetService.evictAllCaches();

        loginCustomUser("wrongWorkspaceId", true);
        resultPage = petriNetService.getByIdentifier(identifier, PageRequest.of(0, 4));
        assertEquals(4, resultPage.getContent().size());
        petriNetService.evictAllCaches();
    }

    @Test
    public void testFindAllById() {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";

        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId1);
        petriNetRepository.save(net);

        PetriNet net2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net2.setWorkspaceId(workspaceId2);
        petriNetRepository.save(net2);

        List<String> ids = List.of(net.getStringId(), net2.getStringId());

        logout();
        List<PetriNet> resultList = petriNetService.findAllById(ids);
        assertEquals(2, resultList.size());

        loginCustomUser(workspaceId1, false);
        resultList = petriNetService.findAllById(ids);
        assertEquals(1, resultList.size());

        loginCustomUser(workspaceId1, true);
        resultList = petriNetService.findAllById(ids);
        assertEquals(2, resultList.size());

        loginCustomUser(workspaceId2, false);
        resultList = petriNetService.findAllById(ids);
        assertEquals(1, resultList.size());

        loginCustomUser(workspaceId2, true);
        resultList = petriNetService.findAllById(ids);
        assertEquals(2, resultList.size());

        loginCustomUser("wrongWorkspaceId", false);
        resultList = petriNetService.findAllById(ids);
        assertEquals(0, resultList.size());

        loginCustomUser("wrongWorkspaceId", true);
        resultList = petriNetService.findAllById(ids);
        assertEquals(2, resultList.size());
    }

    @Test
    public void testGetDefaultVersionByIdentifier() {
        String workspaceId1 = "default";
        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId1);
        net.setIdentifier("identifier");
        net.setDefaultVersion(true);
        petriNetRepository.save(net);

        logout();
        assertNotNull(petriNetService.getDefaultVersionByIdentifier(net.getIdentifier()));
        petriNetService.evictAllCaches();

        loginCustomUser(workspaceId1, false);
        assertNotNull(petriNetService.getDefaultVersionByIdentifier(net.getIdentifier()));
        petriNetService.evictAllCaches();

        loginCustomUser("wrongWorkspaceId", false);
        assertNull(petriNetService.getDefaultVersionByIdentifier(net.getIdentifier()));
        petriNetService.evictAllCaches();

        loginCustomUser("wrongWorkspaceId", true);
        assertNotNull(petriNetService.getDefaultVersionByIdentifier(net.getIdentifier()));
        petriNetService.evictAllCaches();
    }

    @Test
    public void testGetLatestVersionByIdentifier() {
        String workspaceId1 = "default";
        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId1);
        net.setIdentifier("identifier");
        petriNetRepository.save(net);

        logout();
        assertNotNull(petriNetService.getLatestVersionByIdentifier(net.getIdentifier()));
        petriNetService.evictAllCaches();

        loginCustomUser(workspaceId1, false);
        assertNotNull(petriNetService.getLatestVersionByIdentifier(net.getIdentifier()));
        petriNetService.evictAllCaches();

        loginCustomUser("wrongWorkspaceId", false);
        assertNull(petriNetService.getLatestVersionByIdentifier(net.getIdentifier()));
        petriNetService.evictAllCaches();

        loginCustomUser("wrongWorkspaceId", true);
        assertNotNull(petriNetService.getLatestVersionByIdentifier(net.getIdentifier()));
        petriNetService.evictAllCaches();
    }

    @Test
    public void testGetAll() {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";

        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId1);
        petriNetRepository.save(net);

        PetriNet net2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net2.setWorkspaceId(workspaceId2);
        petriNetRepository.save(net2);

        logout();
        Page<PetriNet> resultAsPage = petriNetService.getAll(PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = petriNetService.getAll(PageRequest.of(0, 2));
        assertEquals(1, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, true);
        resultAsPage = petriNetService.getAll(PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = petriNetService.getAll(PageRequest.of(0, 2));
        assertEquals(1, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = petriNetService.getAll(PageRequest.of(0, 2));
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = petriNetService.getAll(PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());
    }

    @Test
    public void testGetAllDefault() {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";

        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId1);
        net.setDefaultVersion(true);
        petriNetRepository.save(net);

        PetriNet net2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net2.setWorkspaceId(workspaceId2);
        net2.setDefaultVersion(true);
        petriNetRepository.save(net2);

        logout();
        Page<PetriNet> resultAsPage = petriNetService.getAllDefault(PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = petriNetService.getAllDefault(PageRequest.of(0, 2));
        assertEquals(1, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, true);
        resultAsPage = petriNetService.getAllDefault(PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = petriNetService.getAllDefault(PageRequest.of(0, 2));
        assertEquals(1, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", false);
        resultAsPage = petriNetService.getAllDefault(PageRequest.of(0, 2));
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultAsPage = petriNetService.getAllDefault(PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());
    }

    @Test
    public void testGetFile() throws IOException, MissingPetriNetMetaDataException {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_version_test.xml"))
                .workspaceId(workspaceId1)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        PetriNet net2 = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_version_test.xml"))
                .workspaceId(workspaceId2)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        logout();
        assertNotNull(petriNetService.getFile(net.getStringId(), null));

        loginCustomUser("wrongWorkspace", false);
        assertNull(petriNetService.getFile(net.getStringId(), null));

        loginCustomUser("wrongWorkspace", true);
        assertNotNull(petriNetService.getFile(net.getStringId(), null));

        loginCustomUser(workspaceId2, false);
        assertNull(petriNetService.getFile(net.getStringId(), null));

        loginCustomUser(workspaceId1, false);
        assertNotNull(petriNetService.getFile(net.getStringId(), null));

        loginCustomUser(workspaceId1, false);
        assertNull(petriNetService.getFile(net2.getStringId(), null));

        loginCustomUser(workspaceId2, false);
        assertNotNull(petriNetService.getFile(net2.getStringId(), null));
    }

    @Test
    public void testGetReferencesByNonNullVersion() throws IOException, MissingPetriNetMetaDataException {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_version_test.xml"))
                .workspaceId(workspaceId1)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        PetriNet net2 = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_version_test.xml"))
                .workspaceId(workspaceId2)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        assertEquals(net.getVersion(), net2.getVersion());
        Version version = new Version();
        assertEquals(version, net.getVersion());

        logout();
        Page<PetriNetReference> resultAsPage = petriNetService.getReferencesByVersion(version, Locale.getDefault(),
                PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspaceId", false);
        resultAsPage = petriNetService.getReferencesByVersion(version, Locale.getDefault(), PageRequest.of(0, 2));
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspaceId", true);
        resultAsPage = petriNetService.getReferencesByVersion(version, Locale.getDefault(), PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultAsPage = petriNetService.getReferencesByVersion(version, Locale.getDefault(), PageRequest.of(0, 2));
        assertEquals(1, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultAsPage = petriNetService.getReferencesByVersion(version, Locale.getDefault(), PageRequest.of(0, 2));
        assertEquals(1, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, true);
        resultAsPage = petriNetService.getReferencesByVersion(version, Locale.getDefault(), PageRequest.of(0, 2));
        assertEquals(2, resultAsPage.getContent().size());
    }

    @Test
    public void testGetReferencesByUsersProcessRoles() throws IOException, MissingPetriNetMetaDataException {
        String workspaceId1 = "default";
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .workspaceId(workspaceId1)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        logout();
        List<PetriNetReference> resultAsList = petriNetService.getReferencesByUsersProcessRoles(Locale.getDefault());
        assertEquals(1, resultAsList.size());

        loginCustomUser("wrongWorkspace", false, new HashSet<>(net.getRoles().values()));
        resultAsList = petriNetService.getReferencesByUsersProcessRoles(Locale.getDefault());
        assertTrue(resultAsList.isEmpty());

        loginCustomUser("wrongWorkspace", true, new HashSet<>(net.getRoles().values()));
        resultAsList = petriNetService.getReferencesByUsersProcessRoles(Locale.getDefault());
        assertEquals(1, resultAsList.size());

        loginCustomUser(workspaceId1, false, new HashSet<>(net.getRoles().values()));
        resultAsList = petriNetService.getReferencesByUsersProcessRoles(Locale.getDefault());
        assertEquals(1, resultAsList.size());
    }

    @Test
    public void testFindByImportId() throws IOException, MissingPetriNetMetaDataException {
        String workspaceId1 = "default";
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .workspaceId(workspaceId1)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        logout();
        assertTrue(petriNetService.findByImportId(net.getIdentifier()).isPresent());

        loginCustomUser("wrongWorkspace", false);
        assertFalse(petriNetService.findByImportId(net.getIdentifier()).isPresent());

        loginCustomUser("wrongWorkspace", true);
        assertTrue(petriNetService.findByImportId(net.getIdentifier()).isPresent());

        loginCustomUser(workspaceId1, false);
        assertTrue(petriNetService.findByImportId(net.getIdentifier()).isPresent());
    }

    @Test
    public void testSearch() throws IOException, MissingPetriNetMetaDataException {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .workspaceId(workspaceId1)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        PetriNet net2 = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .workspaceId(workspaceId2)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        PetriNetSearch searchBody = new PetriNetSearch();
        searchBody.setIdentifier("workspace_test");

        Set<ProcessRole> roles = new HashSet<>();
        roles.addAll(net.getRoles().values());
        roles.addAll(net2.getRoles().values());

        logout();
        Page<PetriNetReference> resultAsPage = petriNetService.search(searchBody, PageRequest.of(0, 2), Locale.getDefault());
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", false, roles);
        resultAsPage = petriNetService.search(searchBody, PageRequest.of(0, 2), Locale.getDefault());
        assertEquals(0, resultAsPage.getContent().size());

        loginCustomUser("wrongWorkspace", true, roles);
        resultAsPage = petriNetService.search(searchBody, PageRequest.of(0, 2), Locale.getDefault());
        assertEquals(2, resultAsPage.getContent().size());

        loginCustomUser(workspaceId1, false, roles);
        resultAsPage = petriNetService.search(searchBody, PageRequest.of(0, 2), Locale.getDefault());
        assertEquals(1, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, false, roles);
        resultAsPage = petriNetService.search(searchBody, PageRequest.of(0, 2), Locale.getDefault());
        assertEquals(1, resultAsPage.getContent().size());

        loginCustomUser(workspaceId2, true, roles);
        resultAsPage = petriNetService.search(searchBody, PageRequest.of(0, 2), Locale.getDefault());
        assertEquals(2, resultAsPage.getContent().size());
    }

    @Test
    public void testGet() throws IOException, MissingPetriNetMetaDataException {
        String workspaceId1 = "default";
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .workspaceId(workspaceId1)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();

        logout();
        PetriNet resultNet = petriNetService.get(net.getObjectId());
        petriNetService.evictAllCaches();
        assertNotNull(resultNet);

        loginCustomUser("wrongWorkspace", false);
        assertThrows(IllegalArgumentException.class, () -> petriNetService.get(net.getObjectId()));
        petriNetService.evictAllCaches();

        loginCustomUser("wrongWorkspace", true);
        resultNet = petriNetService.get(net.getObjectId());
        petriNetService.evictAllCaches();
        assertNotNull(resultNet);

        loginCustomUser(workspaceId1, false);
        resultNet = petriNetService.get(net.getObjectId());
        petriNetService.evictAllCaches();
        assertNotNull(resultNet);
    }

    @Test
    public void testDeletePetriNet() throws IOException, MissingPetriNetMetaDataException {
        String workspaceId1 = "default";
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .workspaceId(workspaceId1)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .author(superCreatorRunner.getSuperUser())
                .build());

        loginCustomUser("wrongWorkspace", false);
        assertThrows(IllegalArgumentException.class, () -> petriNetService.deletePetriNet(net.getStringId()));
        assertTrue(petriNetRepository.findById(net.getStringId()).isPresent());
        assertTrue(caseRepository.exists(QCase.case$.processIdentifier.eq(net.getIdentifier())));

        logout();
        petriNetService.deletePetriNet(net.getStringId());
        assertFalse(petriNetRepository.findById(net.getStringId()).isPresent());
        assertFalse(caseRepository.exists(QCase.case$.processIdentifier.eq(net.getIdentifier())));

        PetriNet net2 = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/workspace_test.xml"))
                .workspaceId(workspaceId1)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        workflowService.createCase(CreateCaseParams.with()
                .process(net2)
                .author(superCreatorRunner.getSuperUser())
                .build());

        loginCustomUser(workspaceId1, false);
        petriNetService.deletePetriNet(net2.getStringId());
        assertFalse(petriNetRepository.findById(net2.getStringId()).isPresent());
        assertFalse(caseRepository.exists(QCase.case$.processIdentifier.eq(net.getIdentifier())));
    }

    @Test
    public void testGetExistingPetriNetIdentifiersFromIdentifiersList() {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";

        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId1);
        net.setIdentifier("identifier1");
        petriNetRepository.save(net);

        PetriNet net2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net2.setWorkspaceId(workspaceId2);
        net2.setIdentifier("identifier2");
        petriNetRepository.save(net2);

        List<String> identifiers = List.of(net.getIdentifier(), net2.getIdentifier());

        logout();
        List<String> existingIdentifiers = petriNetService.getExistingPetriNetIdentifiersFromIdentifiersList(identifiers);
        assertEquals(2, existingIdentifiers.size());

        loginCustomUser("wrongWorkspace", false);
        existingIdentifiers = petriNetService.getExistingPetriNetIdentifiersFromIdentifiersList(identifiers);
        assertEquals(0, existingIdentifiers.size());

        loginCustomUser("wrongWorkspace", true);
        existingIdentifiers = petriNetService.getExistingPetriNetIdentifiersFromIdentifiersList(identifiers);
        assertEquals(2, existingIdentifiers.size());

        loginCustomUser(workspaceId1, false);
        existingIdentifiers = petriNetService.getExistingPetriNetIdentifiersFromIdentifiersList(identifiers);
        assertEquals(1, existingIdentifiers.size());

        loginCustomUser(workspaceId2, false);
        existingIdentifiers = petriNetService.getExistingPetriNetIdentifiersFromIdentifiersList(identifiers);
        assertEquals(1, existingIdentifiers.size());
    }
}
