package com.fmworkflow.workflow.service;

import com.fmworkflow.importer.Importer;
import com.fmworkflow.petrinet.domain.PetriNetRepository;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.CaseRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
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

    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void afterFindOne() throws Exception {
        importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "net", "NET");
        workflowService.createCase(netRepository.findAll().get(0).getStringId(), "Storage Unit", "color-fg-fm-500");

        List<Case> cases = repository.findAll();
        Case useCase = repository.findOne(cases.get(0).getStringId());

        assert !useCase.getPetriNet().isNotInitialized();
    }
}