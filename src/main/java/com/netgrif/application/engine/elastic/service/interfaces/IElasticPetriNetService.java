package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface IElasticPetriNetService {

    @Async
    void index(ElasticPetriNet net);

    void indexNow(ElasticPetriNet net);

    void remove(String id);

    String findUriNodeId(PetriNet net);

    List<PetriNet> findAllByUriNodeId(String uriNodeId);

}
