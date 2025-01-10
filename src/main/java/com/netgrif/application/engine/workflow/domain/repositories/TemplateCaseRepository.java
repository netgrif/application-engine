package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TemplateCaseRepository extends MongoRepository<Case, String> {
    Page<Case> findByProcessIdentifier(String processIdentifier, Pageable pageable);
}
