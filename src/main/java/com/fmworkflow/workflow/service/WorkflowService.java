package com.fmworkflow.workflow.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.Place;
import com.fmworkflow.petrinet.service.IPetriNetService;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.CaseRepository;
import com.fmworkflow.workflow.domain.dataset.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowService implements IWorkflowService {
    @Autowired
    private CaseRepository repository;
    @Autowired
    private IPetriNetService petriNetService;
    @Override
    public void saveCase(Case useCase) {
        repository.save(useCase);
    }

    @Override
    public List<Case> getAll() {
        return repository.findAll();
    }

    @Override
    public void createCase(String netId, String title) {
        PetriNet petriNet = petriNetService.loadPetriNet(netId);
        Map<String, Integer> activePlaces = createActivePlaces(petriNet);
        Case useCase = new Case(title, petriNet, activePlaces);
        useCase.setDataSet(petriNet.getDataSet().copy());
        saveCase(useCase);
    }

    private Map<String, Integer> createActivePlaces(PetriNet petriNet) {
        Map<String, Integer> activePlaces = new HashMap<>();
        Map<String, Place> places = petriNet.getPlaces();
        for (Place place : places.values()) {
            if (place.getTokens() > 0) {
                activePlaces.put(place.getObjectId().toString(), place.getTokens());
            }
        }
        return activePlaces;
    }

    @Override
    public DataSet getDataForTransition(String caseId, String transitionId){
        Case useCase = repository.findOne(caseId);
        return useCase.getDataSet().getFieldsForTransition(transitionId);
    }


}