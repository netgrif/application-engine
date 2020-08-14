package com.netgrif.workflow.petrinet.domain.repositories;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.version.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PetriNetRepository extends MongoRepository<PetriNet, String> {

    List<PetriNet> findByTitle_DefaultValue(String title);

    PetriNet findByImportId(String id);

    List<PetriNet> findAllByIdentifier(String identifier);

    PetriNet findByIdentifierAndVersion(String identifier, Version version);

    Page<PetriNet> findByIdentifier(String identifier, Pageable pageable);

    List<PetriNet> findAllByVersion(Version version);
}