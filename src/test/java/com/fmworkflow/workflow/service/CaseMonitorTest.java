package com.fmworkflow.workflow.service;

import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.CaseRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CaseMonitorTest {
    @Autowired
    private CaseRepository repository;

    @Test
    public void afterFindOne() throws Exception {
        List<Case> cases = repository.findAll();

        Case useCase = repository.findOne(cases.get(0).getStringId());

        assert !useCase.getPetriNet().isNotInitialized();
    }
}