package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.importer.service.Config;
import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class CaseMonitorTest {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private CaseRepository repository;

    @Autowired
    private PetriNetRepository netRepository;

    @Autowired
    private Importer importer;

    @Autowired
    private IAuthorityService authorityService;

    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void afterFindOne() throws Exception {
        importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "net", "NET", new Config());
        workflowService.createCase(netRepository.findAll().get(0).getStringId(), "Storage Unit", "color-fg-fm-500", mockLoggedUser());

        List<Case> cases = repository.findAll();
        Case useCase = repository.findOne(cases.get(0).getStringId());

        assert !useCase.getPetriNet().isNotInitialized();
    }

    public LoggedUser mockLoggedUser(){
        Authority authorityUser = authorityService.getOrCreate(Authority.user);
        return new LoggedUser(1L, "super@netgrif.com","password", Collections.singleton(authorityUser));
    }
}