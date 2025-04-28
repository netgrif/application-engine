package com.netgrif.application.engine.petrinet.domain.repositories;

import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface PetriNetRepository extends MongoRepository<PetriNet, String>, QuerydslPredicateExecutor<PetriNet> {

    List<PetriNet> findByTitle_DefaultValue(String title);

    PetriNet findByImportId(String id);

    List<PetriNet> findAllByIdentifierAndWorkspaceId(String identifier, String workspaceId);

    PetriNet findByIdentifierAndVersionAndWorkspaceId(String identifier, Version version, String workspaceId);

    Page<PetriNet> findByIdentifierAndWorkspaceId(String identifier, String workspaceId, Pageable pageable);

    Page<PetriNet> findByIdentifierInAndWorkspaceId(List<String> identifier, String workspaceId, Pageable pageable);

    List<PetriNet> findAllByVersionAndWorkspaceId(Version version, String workspaceId);

    List<PetriNet> findAllByUriNodeIdAndWorkspaceId(String uri, String workspaceId);

    List<PetriNet> findAllByWorkspaceId(String workspaceId);

    void deleteBy_id(ObjectId id);
}
