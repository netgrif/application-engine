package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

public class PetriNetHashMap extends HashMap<org.bson.types.ObjectId, com.netgrif.workflow.petrinet.domain.PetriNet> {

    @Override
    public PetriNet put(ObjectId key, PetriNet value) {
        Map<String, Field> taskImmediate = new HashMap<>();

        value.getTransitions().values().forEach(trans ->
                trans.getImmediateData().forEach(fieldId -> {
                    if (!taskImmediate.containsKey(fieldId)) {
                        taskImmediate.put(fieldId, value.getDataSet().get(fieldId));
                    }
                }));

        value.setTaskImmediateData(taskImmediate);
        return super.put(key, value);
    }
}
