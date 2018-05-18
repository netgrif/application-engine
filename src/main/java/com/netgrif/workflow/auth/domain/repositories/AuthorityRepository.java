package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    Authority findByName(String name);

    List<Authority> findAllByNameStartsWith(String prefix);
}