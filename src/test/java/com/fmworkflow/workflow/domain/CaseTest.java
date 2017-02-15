package com.fmworkflow.workflow.domain;

import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.Place;
import com.fmworkflow.petrinet.service.IPetriNetService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CaseTest {
    @Autowired
    private IPetriNetService service;
    @Autowired
    private CaseRepository repository;

    private PetriNet petriNet;
    private Map<String, Integer> activePlaces;

    @Before
    public void up() {
        petriNet = service.loadPetriNet("5895bee8b71c6d0eb0649416");
        activePlaces = new HashMap<>();
        Place place = petriNet.getPlaces().values().stream().findFirst().get();
        activePlaces.put(place.getObjectId().toString(), 5);
    }

//    @Test
//    @Ignore
//    public void createCase() {
//        Case useCase = new Case();
//        useCase.setDataSet(new DataSet());
//        useCase.setPetriNet(petriNet);
//        useCase.setTitle("test case");
//        useCase.setActivePlaces(activePlaces);
//
//        repository.save(useCase);
//    }

    @Test
    @Ignore
    public void loadCase() {
        Case useCase = repository.findOne("5895de45b71c6d185c9c1498");
        System.out.println(useCase.getTitle());
    }
}