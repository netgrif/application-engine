package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.dto.response.petrinet.PetriNetReferenceDto;
import com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.Locale;

public interface IElasticPetriNetService {

    @Async
    void index(ElasticPetriNet net);

    void indexNow(ElasticPetriNet net);

    void remove(String id);

    Page<PetriNetReferenceDto> search(PetriNetSearch requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection);

}
