package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(String name);
}