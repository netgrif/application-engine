package com.netgrif.workflow.psc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostalCodeRepository extends JpaRepository<PostalCode,String> {
    PostalCode findByCode(String code);
    List<PostalCode> findByLocality(String locality);
}
