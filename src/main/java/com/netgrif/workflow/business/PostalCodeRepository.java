package com.netgrif.workflow.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostalCodeRepository extends JpaRepository<PostalCode, Long> {

    List<PostalCode> findAllByCode(String code);

    List<PostalCode> findAllByCity(String city);
}