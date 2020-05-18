package com.netgrif.workflow.rules.domain;

import com.netgrif.workflow.rules.domain.facts.RuleFact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RuleFactRepository extends MongoRepository<RuleFact, String> {

}