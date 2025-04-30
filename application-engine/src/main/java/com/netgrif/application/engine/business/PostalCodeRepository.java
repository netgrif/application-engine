package com.netgrif.application.engine.business;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostalCodeRepository extends MongoRepository<PostalCode, String> {

    List<PostalCode> findAllByCode(String code);

    List<PostalCode> findAllByCity(String city);
}