package com.netgrif.application.engine.workspace;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.elastic.domain.ElasticPetriNetRepository;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
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
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkspaceElasticPetriNetTest {

    @Autowired
    private IElasticPetriNetService elasticPetriNetService;

    @Autowired
    private ElasticPetriNetRepository elasticPetriNetRepository;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
        elasticPetriNetRepository.deleteAll();
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
    public void testSearch() {
        String workspaceId = "workspace1";

        PetriNet petriNet = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        petriNet.setWorkspaceId(workspaceId);
        petriNet.setIdentifier("identifier1");
        petriNet = petriNetRepository.save(petriNet);
        elasticPetriNetRepository.save(new ElasticPetriNet(petriNet));

        PetriNetSearch search = new PetriNetSearch();
        search.setIdentifier(petriNet.getIdentifier());

        logout();
        Page<PetriNetReference> resultPage = elasticPetriNetService.search(search, PageRequest.of(0, 1), Locale.getDefault(), false);
        assertEquals(1, resultPage.getContent().size());
        assertEquals(resultPage.getContent().getFirst().getStringId(), petriNet.getStringId());

        loginCustomUser("wrongWorkspace", false);
        resultPage = elasticPetriNetService.search(search, PageRequest.of(0, 1), Locale.getDefault(), false);
        assertEquals(0, resultPage.getContent().size());

        loginCustomUser("wrongWorkspace", true);
        resultPage = elasticPetriNetService.search(search, PageRequest.of(0, 1), Locale.getDefault(), false);
        assertEquals(1, resultPage.getContent().size());
        assertEquals(resultPage.getContent().getFirst().getStringId(), petriNet.getStringId());

        loginCustomUser(workspaceId, false);
        resultPage = elasticPetriNetService.search(search, PageRequest.of(0, 1), Locale.getDefault(), false);
        assertEquals(1, resultPage.getContent().size());
        assertEquals(resultPage.getContent().getFirst().getStringId(), petriNet.getStringId());
    }
}
