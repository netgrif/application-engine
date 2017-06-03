package com.netgrif.workflow.auth.domain.repositories;


import com.netgrif.workflow.auth.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long>{
}
