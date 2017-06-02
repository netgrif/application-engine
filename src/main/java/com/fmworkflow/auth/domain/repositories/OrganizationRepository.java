package com.fmworkflow.auth.domain.repositories;


import com.fmworkflow.auth.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long>{
}
