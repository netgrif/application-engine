package com.netgrif.workflow.business;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostalCodeRepository extends MongoRepository<PostalCode, String>, QuerydslPredicateExecutor<PostalCode> {

    List<PostalCode> findAllByCode(String code);

    List<PostalCode> findAllByCity(String city);
}