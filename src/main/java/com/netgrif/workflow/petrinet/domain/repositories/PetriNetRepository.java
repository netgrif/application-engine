package com.netgrif.workflow.petrinet.domain.repositories;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PetriNetRepository extends MongoRepository<PetriNet, String> {

    List<PetriNet> findByTitle_DefaultValue(String title);

    PetriNet findByImportId(Long id);

    List<PetriNet> findAllByIdentifier(String identifier);

    PetriNet findByIdentifierAndVersion(String identifier, String version);

    Page<PetriNet> findByIdentifier(String identifier, Pageable pageable);

    List<PetriNet> findAllByVersion(String version);
}