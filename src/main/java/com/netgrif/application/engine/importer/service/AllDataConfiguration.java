package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.*;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class AllDataConfiguration {

    private Transition allData;
// TODO: release/8.0.0 implement logic of creating all data transition here, move from importer

    public Transition createAllDataTransition(Document document) {
        Transition allDataConfig = this.getAllData();
        if (document.getTransition().stream().anyMatch(transition -> allDataConfig.getId().equals(transition.getId()))) {
            return null;
        }
        Transition allDataTransition = new com.netgrif.application.engine.importer.model.Transition();
        allDataTransition.setId(allDataConfig.getId());
        allDataTransition.setX(allDataConfig.getX());
        allDataTransition.setY(allDataConfig.getY());
        allDataTransition.setLabel(allDataConfig.getLabel());
        allDataTransition.setIcon(allDataConfig.getIcon());
        allDataTransition.setPriority(allDataConfig.getPriority());
        allDataTransition.setAssignPolicy(allDataConfig.getAssignPolicy());
        allDataTransition.setFinishPolicy(allDataConfig.getFinishPolicy());
        GridContainer gridContainer = new GridContainer();
        gridContainer.setId(allDataConfig.getGrid().getId());
        gridContainer.setProperties(allDataConfig.getGrid().getProperties());
        // TODO: NAE-1858: all properties
        for (com.netgrif.application.engine.importer.model.Data field : document.getData()) {
            GridItem gridItem = new GridItem();
            DataRef dataRef = new DataRef();
            dataRef.setId(field.getId());
            Logic logic = new Logic();
            logic.getBehavior().add(Behavior.EDITABLE);
            dataRef.setLogic(logic);
            gridItem.setDataRef(dataRef);
            gridContainer.getItem().add(gridItem);
        }
        allDataTransition.setGrid(gridContainer);
        return allDataTransition;
    }
}
