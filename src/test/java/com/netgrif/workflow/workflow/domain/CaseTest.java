package com.netgrif.workflow.workflow.domain;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Place;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ActiveProfiles({"test"})
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
        petriNet = service.getPetriNet("5895bee8b71c6d0eb0649416");
        activePlaces = new HashMap<>();
        Place place = petriNet.getPlaces().values().stream().findFirst().get();
        activePlaces.put(place.getStringId(), 5);
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