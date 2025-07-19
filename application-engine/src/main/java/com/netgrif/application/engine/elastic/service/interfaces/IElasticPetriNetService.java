package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet;
import org.springframework.scheduling.annotation.Async;

public interface IElasticPetriNetService {

    @Async
    void index(ElasticPetriNet net);

    void indexNow(ElasticPetriNet net);

    void remove(String id);
}
