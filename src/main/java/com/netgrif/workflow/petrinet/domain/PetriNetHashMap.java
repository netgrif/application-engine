package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PetriNetHashMap extends HashMap<org.bson.types.ObjectId, com.netgrif.workflow.petrinet.domain.PetriNet> {

    @Override
    public PetriNet put(ObjectId key, PetriNet value) {
         value.setTaskImmediateData(value.getTransitions().values().stream().map(Transition::getImmediateData)
                .flatMap(List::stream).sorted().distinct()
                .collect(Collectors.toMap(Function.identity(), field -> value.getDataSet().get(field))));
        return super.put(key, value);
    }
}
