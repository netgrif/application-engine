package com.fmworkflow.auth.domain.repositories;

import com.fmworkflow.auth.domain.Organization;
import com.fmworkflow.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findBySurname(String surname);

    List<User> findByOrganizationsIn(List<Organization> orgs);
}