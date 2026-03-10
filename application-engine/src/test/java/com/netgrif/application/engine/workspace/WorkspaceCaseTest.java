package com.netgrif.application.engine.workspace;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.QCase;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkspaceCaseTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private IWorkflowService workflowService;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
        petriNetRepository.deleteAll();
        caseRepository.deleteAll();
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
    public void testSave() {
        String workspaceId = "default";
        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId);
        net = petriNetRepository.save(net);

        Case useCase = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net);
        useCase.setWorkspaceId(workspaceId);
        assertNotNull(workflowService.save(useCase));

        loginCustomUser("differentWorkspaceId", false);
        assertThrows(IllegalArgumentException.class, () -> workflowService.save(useCase));

        loginCustomUser(net.getWorkspaceId(), false);
        assertNotNull(workflowService.save(useCase));

        loginCustomUser("differentWorkspaceId", true);
        assertNotNull(workflowService.save(useCase));

        logout();
        assertNotNull(workflowService.save(useCase));

        loginCustomUser(null, false);
        assertThrows(IllegalArgumentException.class, () -> workflowService.save(useCase));
    }

    @Test
    public void testFindOne() {
        String workspaceId = "default";
        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId);
        net = petriNetRepository.save(net);

        Case useCase = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net);
        useCase.setWorkspaceId(workspaceId);
        caseRepository.save(useCase);

        logout();
        assertNotNull(workflowService.findOne(useCase.getStringId()));

        loginCustomUser("differentWorkspaceId", false);
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(useCase.getStringId()));

        loginCustomUser(workspaceId, false);
        assertNotNull(workflowService.findOne(useCase.getStringId()));

        loginCustomUser("differentWorkspaceId", true);
        assertNotNull(workflowService.findOne(useCase.getStringId()));
    }

    @Test
    public void testFindOneNoNet() {
        String workspaceId = "default";
        PetriNet net = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net.setWorkspaceId(workspaceId);
        net = petriNetRepository.save(net);
        Case useCase = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net);
        useCase.setWorkspaceId(workspaceId);
        caseRepository.save(useCase);

        logout();
        assertNotNull(workflowService.findOneNoNet(useCase.getStringId()));

        loginCustomUser("differentWorkspaceId", false);
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOneNoNet(useCase.getStringId()));

        loginCustomUser(workspaceId, false);
        assertNotNull(workflowService.findOneNoNet(useCase.getStringId()));

        loginCustomUser("differentWorkspaceId", true);
        assertNotNull(workflowService.findOneNoNet(useCase.getStringId()));
    }

    @Test
    public void testFindAllById() {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";
        PetriNet net1 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net1.setWorkspaceId(workspaceId1);
        net1 = petriNetRepository.save(net1);
        Case useCase1 = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net1);
        useCase1.setWorkspaceId(workspaceId1);
        caseRepository.save(useCase1);

        PetriNet net2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net2.setWorkspaceId(workspaceId2);
        net2 = petriNetRepository.save(net2);
        Case useCase2 = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net2);
        useCase2.setWorkspaceId(workspaceId2);
        caseRepository.save(useCase2);

        List<String> ids = List.of(useCase1.getStringId(), useCase2.getStringId());

        logout();
        List<Case> resultList = workflowService.findAllById(ids);
        assertEquals(2, resultList.size());

        loginCustomUser(workspaceId1, false);
        resultList = workflowService.findAllById(ids);
        assertEquals(1, resultList.size());

        loginCustomUser(workspaceId1, true);
        resultList = workflowService.findAllById(ids);
        assertEquals(2, resultList.size());

        loginCustomUser(workspaceId2, false);
        resultList = workflowService.findAllById(ids);
        assertEquals(1, resultList.size());

        loginCustomUser(workspaceId2, true);
        resultList = workflowService.findAllById(ids);
        assertEquals(2, resultList.size());

        loginCustomUser("wrongWorkspaceId", false);
        resultList = workflowService.findAllById(ids);
        assertEquals(0, resultList.size());

        loginCustomUser("wrongWorkspaceId", true);
        resultList = workflowService.findAllById(ids);
        assertEquals(2, resultList.size());
    }

    @Test
    public void testGetAll() {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";
        PetriNet net1 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net1.setWorkspaceId(workspaceId1);
        net1 = petriNetRepository.save(net1);
        Case useCase1 = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net1);
        useCase1.setWorkspaceId(workspaceId1);
        caseRepository.save(useCase1);

        PetriNet net2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net2.setWorkspaceId(workspaceId2);
        net2 = petriNetRepository.save(net2);
        Case useCase2 = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net2);
        useCase2.setWorkspaceId(workspaceId2);
        caseRepository.save(useCase2);

        Pageable pageable = PageRequest.of(0, 2);

        logout();
        Page<Case> resultPage = workflowService.getAll(pageable);
        assertEquals(2, resultPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultPage = workflowService.getAll(pageable);
        assertEquals(1, resultPage.getContent().size());

        loginCustomUser(workspaceId1, true);
        resultPage = workflowService.getAll(pageable);
        assertEquals(2, resultPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultPage = workflowService.getAll(pageable);
        assertEquals(1, resultPage.getContent().size());

        loginCustomUser(workspaceId2, true);
        resultPage = workflowService.getAll(pageable);
        assertEquals(2, resultPage.getContent().size());

        loginCustomUser("wrongWorkspaceId", false);
        resultPage = workflowService.getAll(pageable);
        assertEquals(0, resultPage.getContent().size());

        loginCustomUser("wrongWorkspaceId", true);
        resultPage = workflowService.getAll(pageable);
        assertEquals(2, resultPage.getContent().size());
    }

    @Test
    public void testResolveActorRef() {
    }

    @Test
    public void testCreateCase() {
    }

    @Test
    public void testFindAllByAuthor() {
    }

    @Test
    public void testDeleteCase() {
    }

    @Test
    public void testDeleteSubtreeRootedAt() {
    }

    @Test
    public void testDeleteInstancesOfPetriNet() {
    }

    @Test
    public void testDeleteInstancesOfPetriNetWithForce() {
    }

    @Test
    public void testUpdateMarking() {
    }

    @Test
    public void testSearchAll() {
        String workspaceId1 = "default";
        String workspaceId2 = "otherWorkspaceId";
        String title = "title";
        PetriNet net1 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net1.setWorkspaceId(workspaceId1);
        net1 = petriNetRepository.save(net1);
        Case useCase1 = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net1);
        useCase1.setTitle(title);
        useCase1.setWorkspaceId(workspaceId1);
        caseRepository.save(useCase1);

        PetriNet net2 = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        net2.setWorkspaceId(workspaceId2);
        net2 = petriNetRepository.save(net2);
        Case useCase2 = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(net2);
        useCase2.setTitle(title);
        useCase2.setWorkspaceId(workspaceId2);
        caseRepository.save(useCase2);

        Predicate predicate = QCase.case$.title.eq("title");

        logout();
        Page<Case> resultPage = workflowService.searchAll(predicate);
        assertEquals(2, resultPage.getContent().size());

        loginCustomUser(workspaceId1, false);
        resultPage = workflowService.searchAll(predicate);
        assertEquals(1, resultPage.getContent().size());

        loginCustomUser(workspaceId1, true);
        resultPage = workflowService.searchAll(predicate);
        assertEquals(2, resultPage.getContent().size());

        loginCustomUser(workspaceId2, false);
        resultPage = workflowService.searchAll(predicate);
        assertEquals(1, resultPage.getContent().size());

        loginCustomUser("wrongWorkspace", false);
        resultPage = workflowService.searchAll(predicate);
        assertEquals(0, resultPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultPage = workflowService.searchAll(predicate);
        assertEquals(2, resultPage.getContent().size());
    }

    @Test
    public void testSearchOne() {
    }

    @Test
    public void testListToMap() {
    }

    @Test
    public void testSearch() {
    }

    @Test
    public void testCount() {
    }

    @Test
    public void testRemoveTasksFromCaseById() {
    }

    @Test
    public void testRemoveTasksFromCaseByCase() {
    }

    @Test
    public void testSearchWithPredicate() {
    }

    @Test
    public void testSetPetriNet() {
    }
}
