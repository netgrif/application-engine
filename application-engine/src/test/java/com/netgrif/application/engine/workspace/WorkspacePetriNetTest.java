package com.netgrif.application.engine.workspace;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
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
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private WorkspaceService workspaceService;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
        petriNetRepository.deleteAll();
    }

    private void loginCustomUser(String activeWorkspaceId, boolean isAdmin) {
        LoggedUser loggedUser = new LoggedUserImpl();
        loggedUser.setUsername("username1");
        loggedUser.setActiveWorkspaceId(activeWorkspaceId);
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
        // todo 2072 import same version with different workspaces -> should be ok
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
//
//    Page<PetriNet> getAll(Pageable pageable);
//
//    Page<PetriNet> getAllDefault(Pageable pageable);
//
//    FileSystemResource getFile(String netId, String title);
//
//    Page<PetriNetReference> getReferences(Locale locale, Pageable pageable);
//
//    Page<PetriNetReference> getReferencesByIdentifier(String identifier, Locale locale, Pageable pageable);
//
//    Page<PetriNetReference> getReferencesByVersion(Version version, Locale locale, Pageable pageable);
//
//    List<PetriNetReference> getReferencesByUsersProcessRoles(Locale locale);
//
//    PetriNetReference getReference(String identifier, Version version, Locale locale);
//
//    List<TransitionReference> getTransitionReferences(List<String> netsIds, Locale locale);
//
//    List<com.netgrif.application.engine.petrinet.web.responsebodies.DataFieldReference> getDataFieldReferences(List<TransitionReference> transitions, Locale locale);
//
//    Page<PetriNetReference> search(PetriNetSearch criteria, Pageable pageable, Locale locale);
//
//    Optional<PetriNet> findByImportId(String id);
//
//    PetriNet get(ObjectId petriNetId);
//
//    List<PetriNet> get(Collection<ObjectId> petriNetId);
//
//    List<PetriNet> get(List<String> petriNetIds);
//
//    void deletePetriNet(DeletePetriNetParams deletePetriNetParams);
//
//    void forceDeletePetriNet(DeletePetriNetParams deletePetriNetParams);
//
//    List<String> getExistingPetriNetIdentifiersFromIdentifiersList(List<String> identifiers);
//
//    PetriNetImportReference getNetFromCase(String caseId);
//
//    Page<PetriNet> findAllByRoleId(String roleId, Pageable pageable);
}
