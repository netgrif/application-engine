package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.workflow.domain.TemplateCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TemplateCaseRepository extends MongoRepository<TemplateCase, String> {
    Page<TemplateCase> findByProcessIdentifier(String processIdentifier, Pageable pageable);
}
