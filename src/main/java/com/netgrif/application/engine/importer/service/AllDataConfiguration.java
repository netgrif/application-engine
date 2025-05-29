package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.DataRef;
import com.netgrif.application.engine.importer.model.DataRefLogic;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class AllDataConfiguration {

    private com.netgrif.application.engine.importer.model.Transition allData;
// TODO: release/8.0.0 implement logic of creating all data transition here, move from importer

    public com.netgrif.application.engine.importer.model.Transition createAllDataTransition(com.netgrif.application.engine.importer.model.Process document) {
        com.netgrif.application.engine.importer.model.Transition allDataConfig = this.getAllData();
        if (document.getTransition().stream().anyMatch(transition -> allDataConfig.getId().equals(transition.getId()))) {
            return null;
        }
        com.netgrif.application.engine.importer.model.Transition allDataTransition = new com.netgrif.application.engine.importer.model.Transition();
        allDataTransition.setId(allDataConfig.getId());
        allDataTransition.setX(allDataConfig.getX());
        allDataTransition.setY(allDataConfig.getY());
        allDataTransition.setTitle(allDataConfig.getTitle());
        allDataTransition.setIcon(allDataConfig.getIcon());
        allDataTransition.setAssignPolicy(allDataConfig.getAssignPolicy());
        allDataTransition.setFinishPolicy(allDataConfig.getFinishPolicy());
        com.netgrif.application.engine.importer.model.GridContainer gridContainer = new com.netgrif.application.engine.importer.model.GridContainer();
        gridContainer.setId(allDataConfig.getGrid().getId());
        gridContainer.setProperties(allDataConfig.getGrid().getProperties());
        // TODO: NAE-1858: all properties
        for (com.netgrif.application.engine.importer.model.Data field : document.getData()) {
            com.netgrif.application.engine.importer.model.GridItem gridItem = new com.netgrif.application.engine.importer.model.GridItem();
            DataRef dataRef = new DataRef();
            dataRef.setId(field.getId());
            DataRefLogic logic = new DataRefLogic();
            logic.setBehavior(com.netgrif.application.engine.importer.model.Behavior.EDITABLE);
            dataRef.setLogic(logic);
            gridItem.setDataRef(dataRef);
            gridContainer.getItem().add(gridItem);
        }
        allDataTransition.setGrid(gridContainer);
        return allDataTransition;
    }
}
