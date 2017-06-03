package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}