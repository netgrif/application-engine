package com.fmworkflow.workflow.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.service.interfaces.IPetriNetService;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.repositories.CaseRepository;
import com.fmworkflow.workflow.service.interfaces.ITaskService;
import com.fmworkflow.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowService implements IWorkflowService {
    @Autowired
    private CaseRepository repository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private ITaskService taskService;

    @Override
    public void saveCase(Case useCase) {
        repository.save(useCase);
    }

    @Override
    public Page<Case> getAll(Pageable pageable) {
        Page<Case> page = repository.findAll(pageable);
        page.getContent().forEach(aCase -> aCase.getPetriNet().initializeArcs());
        return page;
    }

    public Page<Case> searchCase(List<String> nets, Pageable pageable){
        StringBuilder queryBuilder = new StringBuilder();
        nets.forEach(net -> {
            queryBuilder.append("{$ref:\"petriNet\",$id:{$oid:\"");
            queryBuilder.append(net);
            queryBuilder.append("\"}},");
        });
        if(queryBuilder.length() > 0)
            queryBuilder.deleteCharAt(queryBuilder.length()-1);
        String queryString = nets.isEmpty() ? "{}" : "{petriNet:{$in:["+queryBuilder.toString()+"]}}";
        BasicQuery query = new BasicQuery(queryString,"{petriNet:0, dataSetValues:0}");
        query = (BasicQuery) query.with(pageable);
        List<Case> useCases = mongoTemplate.find(query,Case.class);
        return new PageImpl<Case>(useCases,pageable,mongoTemplate.count(new BasicQuery(queryString,"{petriNet:0, dataSetValues:0}"),Case.class));
    }

    @Override
    public void createCase(String netId, String title, String color) {
        PetriNet petriNet = petriNetService.loadPetriNet(netId);
        Map<String, Integer> activePlaces = petriNet.getActivePlaces();
        Case useCase = new Case(title, petriNet, activePlaces);
        useCase.setColor(color);
        HashMap<String, Object> dataValues = new HashMap<>();
        petriNet.getDataSet().forEach((key, field) -> dataValues.put(key,null));
        useCase.setDataSetValues(dataValues);
        saveCase(useCase);
        taskService.createTasks(useCase);
    }
}