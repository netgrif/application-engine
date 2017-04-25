package com.fmworkflow.workflow.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.service.interfaces.IPetriNetService;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.repositories.CaseRepository;
import com.fmworkflow.workflow.service.interfaces.ITaskService;
import com.fmworkflow.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Case> getAll() {
        List<Case> cases = repository.findAll();
        cases.forEach(aCase -> aCase.getPetriNet().initializeArcs());
        return cases;
    }

    public List<Case> searchCase(List<String> nets){
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
        return mongoTemplate.find(query,Case.class);
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

//    @Override
//    public DataSet getDataForTransition(String caseId, String transitionId){
//        Case useCase = repository.findOne(caseId);
//        return useCase.getDataSet().getFieldsForTransition(transitionId);
//    }

//    @Override
//    public void modifyData(String caseId, Map<String, String> newValues){
//        Case useCase = repository.findOne(caseId);
//        for(Field field:useCase.getDataSet().getFields()){
//            if(newValues.containsKey(field.get_id().toString())){
//                field.modifyValue(newValues.get(field.get_id().toString()));
//            }
//        }
//
//        saveCase(useCase);
//    }


}